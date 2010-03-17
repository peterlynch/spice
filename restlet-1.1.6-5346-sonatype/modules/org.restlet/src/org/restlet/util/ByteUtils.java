/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.util;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.resource.Representation;
import org.restlet.resource.WriterRepresentation;

/**
 * Byte manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public final class ByteUtils {

    /**
     * Input stream connected to a non-blocking readable channel.
     */
    private final static class NbChannelInputStream extends InputStream {
        private final ByteBuffer bb;

        /** The channel to read from. */
        private final ReadableByteChannel channel;

        /** Indicates if further reads can be attempted. */
        private boolean endReached;

        /** The selectable channel to read from. */
        private final SelectableChannel selectableChannel;

        /**
         * Constructor.
         * 
         * @param channel
         *            The channel to read from.
         */
        public NbChannelInputStream(ReadableByteChannel channel) {
            this.channel = channel;

            if (channel instanceof SelectableChannel) {
                this.selectableChannel = (SelectableChannel) channel;
            } else {
                this.selectableChannel = null;
            }

            this.bb = ByteBuffer.allocate(8192);
            this.bb.flip();
            this.endReached = false;
        }

        @Override
        public int read() throws IOException {
            int result = -1;

            if (!this.endReached) {
                if (!this.bb.hasRemaining()) {
                    // Let's refill
                    refill();
                }

                if (!this.endReached) {
                    // Let's return the next one
                    result = this.bb.get() & 0xff;
                }
            }

            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int result = -1;

            if (!this.endReached) {
                if (!this.bb.hasRemaining() && (this.selectableChannel != null)) {
                    // Let's try to refill
                    refill();
                }

                if (!this.endReached) {
                    // Let's return the next ones
                    result = Math.min(len, this.bb.remaining());
                    this.bb.get(b, off, result);
                }
            }

            return result;
        }

        /**
         * Reads the available bytes from the channel into the byte buffer.
         * 
         * @return The number of bytes read or -1 if the end of channel has been
         *         reached.
         * @throws IOException
         */
        private int readChannel() throws IOException {
            int result = 0;
            this.bb.clear();
            result = this.channel.read(this.bb);
            this.bb.flip();
            return result;
        }

        /**
         * Refill the byte buffer by attempting to read the channel.
         * 
         * @throws IOException
         */
        private void refill() throws IOException {
            // No, let's try to read more
            Selector selector = null;
            SelectionKey selectionKey = null;

            try {
                int bytesRead = readChannel();

                // If no bytes were read, try to register a select key to
                // get more
                if (bytesRead == 0) {
                    selector = SelectorFactory.getSelector();

                    if (selector != null) {
                        selectionKey = this.selectableChannel.register(
                                selector, SelectionKey.OP_READ);
                        selector.select(NIO_TIMEOUT);
                    }

                    bytesRead = readChannel();
                } else if (bytesRead == -1) {
                    this.endReached = true;
                }
            } finally {
                release(selector, selectionKey);
            }
        }
    }

    /**
     * Output stream connected to a non-blocking writable channel.
     */
    private final static class NbChannelOutputStream extends OutputStream {
        private final ByteBuffer bb = ByteBuffer.allocate(8192);

        /** The channel to write to. */
        private final WritableByteChannel channel;

        /** The selectable channel to write to. */
        private final SelectableChannel selectableChannel;

        private SelectionKey selectionKey;

        private Selector selector;

        /**
         * Constructor.
         * 
         * @param channel
         *            The wrapped channel.
         */
        public NbChannelOutputStream(WritableByteChannel channel) {
            if (channel instanceof SelectableChannel) {
                this.selectableChannel = (SelectableChannel) channel;
            } else {
                this.selectableChannel = null;
            }

            this.channel = channel;
            this.selector = null;
        }

        /**
         * Effectively write the current byte buffer.
         * 
         * @throws IOException
         */
        private void doWrite() throws IOException {
            if ((this.channel != null) && (this.bb != null)) {
                try {
                    int bytesWritten;
                    int attempts = 0;

                    while (this.bb.hasRemaining()) {
                        bytesWritten = this.channel.write(this.bb);
                        attempts++;

                        if (bytesWritten < 0) {
                            throw new EOFException(
                                    "Unexpected negative number of bytes written. End of file detected.");
                        } else if (bytesWritten == 0) {
                            if (this.selectableChannel != null) {
                                if (this.selector == null) {
                                    this.selector = SelectorFactory
                                            .getSelector();
                                }

                                if (this.selector == null) {
                                    if (attempts > 2) {
                                        throw new IOException(
                                                "Unable to obtain a selector. Selector factory returned null.");
                                    }
                                } else {
                                    // Register a selector to write more
                                    this.selectionKey = this.selectableChannel
                                            .register(this.selector,
                                                    SelectionKey.OP_WRITE);

                                    if (this.selector.select(NIO_TIMEOUT) == 0) {
                                        if (attempts > 2) {
                                            throw new IOException(
                                                    "Unable to select the channel to write to it. Selection timed out.");
                                        }
                                    } else {
                                        attempts--;
                                    }
                                }
                            }
                        } else {
                            attempts = 0;
                        }
                    }
                } catch (IOException ioe) {
                    throw new IOException(
                            "Unable to write to the non-blocking channel. "
                                    + ioe.getLocalizedMessage());
                } finally {
                    this.bb.clear();
                    release(this.selector, this.selectionKey);
                }
            } else {
                throw new IOException(
                        "Unable to write. Null byte buffer or channel detected.");
            }
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            this.bb.clear();
            this.bb.put(b, off, len);
            this.bb.flip();
            doWrite();
        }

        @Override
        public void write(int b) throws IOException {
            this.bb.clear();
            this.bb.put((byte) b);
            this.bb.flip();
            doWrite();
        }
    }

    /**
     * Pipe stream that pipes output streams into input streams. Implementation
     * based on a shared synchronized queue.
     * 
     * @author Jerome Louvel
     */
    private final static class PipeStream {
        private static final long QUEUE_TIMEOUT = 5;

        /** The supporting synchronized queue. */
        private final BlockingQueue<Integer> queue;

        /** Constructor. */
        public PipeStream() {
            this.queue = new ArrayBlockingQueue<Integer>(1024);
        }

        /**
         * Returns a new input stream that can read from the pipe.
         * 
         * @return A new input stream that can read from the pipe.
         */
        public InputStream getInputStream() {
            return new InputStream() {
                private boolean endReached = false;

                @Override
                public int read() throws IOException {
                    try {
                        if (this.endReached) {
                            return -1;
                        }

                        final Integer value = PipeStream.this.queue.poll(
                                QUEUE_TIMEOUT, TimeUnit.SECONDS);
                        if (value == null) {
                            throw new IOException(
                                    "Timeout while reading from the queue-based input stream");
                        }

                        this.endReached = (value.intValue() == -1);
                        return value;
                    } catch (InterruptedException ie) {
                        throw new IOException(
                                "Interruption occurred while writing in the queue");
                    }
                }
            };
        }

        /**
         * Returns a new output stream that can write into the pipe.
         * 
         * @return A new output stream that can write into the pipe.
         */
        public OutputStream getOutputStream() {
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    try {
                        if (!PipeStream.this.queue.offer(b, QUEUE_TIMEOUT,
                                TimeUnit.SECONDS)) {
                            throw new IOException(
                                    "Timeout while writing to the queue-based output stream");
                        }
                    } catch (InterruptedException ie) {
                        throw new IOException(
                                "Interruption occurred while writing in the queue");
                    }
                }
            };
        }

    }

    /**
     * Input stream based on a reader.
     */
    private static class ReaderInputStream extends InputStream {
        /**
         * Writer to an output stream that converts characters according to a
         * given character set.
         */
        private final OutputStreamWriter outputStreamWriter;

        /** Input stream that gets its content from the piped output stream. */
        private final PipedInputStream pipedInputStream;

        /** Output stream that sends its content to the piped input stream. */
        private final PipedOutputStream pipedOutputStream;

        /** The wrapped reader. */
        private final BufferedReader reader;

        /**
         * Constructor.
         * 
         * @param reader
         * @param characterSet
         * @throws IOException
         */
        public ReaderInputStream(Reader reader, CharacterSet characterSet)
                throws IOException {
            this.reader = (reader instanceof BufferedReader) ? (BufferedReader) reader
                    : new BufferedReader(reader);
            this.pipedInputStream = new PipedInputStream();
            this.pipedOutputStream = new PipedOutputStream(
                    this.pipedInputStream);

            if (characterSet != null) {
                this.outputStreamWriter = new OutputStreamWriter(
                        this.pipedOutputStream, characterSet.getName());
            } else {
                this.outputStreamWriter = new OutputStreamWriter(
                        this.pipedOutputStream);
            }
        }

        @Override
        public int available() throws IOException {
            return this.pipedInputStream.available();
        }

        @Override
        public void close() throws IOException {
            this.reader.close();
            this.outputStreamWriter.close();
            this.pipedInputStream.close();
        }

        @Override
        public int read() throws IOException {
            int result = -1;

            if (this.pipedInputStream.available() == 0) {
                int character = this.reader.read();

                if (character != -1) {
                    this.outputStreamWriter.write(character);
                    this.outputStreamWriter.flush();
                    this.pipedOutputStream.flush();
                    result = this.pipedInputStream.read();
                }
            } else {
                result = this.pipedInputStream.read();
            }

            return result;
        }
    }

    /**
     * Factory used to dispatch/share <code>Selector</code>.
     * 
     * @author Jean-Francois Arcand
     */
    private final static class SelectorFactory {
        /**
         * The number of <code>Selector</code> to create.
         */
        public static int maxSelectors = 20;

        /**
         * Cache of <code>Selector</code>
         */
        private final static Stack<Selector> selectors = new Stack<Selector>();

        /**
         * The timeout before we exit.
         */
        public static long timeout = 5000;

        /**
         * Creates the <code>Selector</code>
         */
        static {
            try {
                for (int i = 0; i < maxSelectors; i++) {
                    selectors.add(Selector.open());
                }
            } catch (IOException ex) {
                // do nothing.
            }
        }

        /**
         * Get a exclusive <code>Selector</code>
         * 
         * @return <code>Selector</code>
         */
        public final static Selector getSelector() {
            synchronized (selectors) {
                Selector selector = null;

                try {
                    if (selectors.size() != 0) {
                        selector = selectors.pop();
                    }
                } catch (EmptyStackException ex) {
                }

                int attempts = 0;
                try {
                    while ((selector == null) && (attempts < 2)) {
                        selectors.wait(timeout);

                        try {
                            if (selectors.size() != 0) {
                                selector = selectors.pop();
                            }
                        } catch (EmptyStackException ex) {
                            break;
                        }

                        attempts++;
                    }
                } catch (InterruptedException ex) {
                }

                return selector;
            }
        }

        /**
         * Return the <code>Selector</code> to the cache
         * 
         * @param s
         *            <code>Selector</code>
         */
        public final static void returnSelector(Selector s) {
            synchronized (selectors) {
                selectors.push(s);
                if (selectors.size() == 1) {
                    selectors.notify();
                }
            }
        }
    }

    /**
     * Outputstream wrapping a character writer.
     * 
     * @author Kevin Conaway
     */
    private final static class WriterOutputStream extends OutputStream {
        private final Writer writer;

        public WriterOutputStream(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.writer.close();
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            this.writer.flush();
        }

        @Override
        public void write(int b) throws IOException {
            this.writer.write(b);
        }
    }

    private static final int NIO_TIMEOUT = 60000;

    /**
     * Exhauts the content of the representation by reading it and silently
     * discarding anything read.
     * 
     * @param input
     *            The input stream to exhaust.
     * @return The number of bytes consumed or -1 if unknown.
     */
    public static long exhaust(InputStream input) throws IOException {
        long result = -1L;

        if (input != null) {
            final byte[] buf = new byte[4096];
            int read = input.read(buf);
            result = (read == -1) ? -1 : 0;

            while (read != -1) {
                result += read;
                read = input.read(buf);
            }
        }

        return result;
    }

    /**
     * Returns a readable byte channel based on a given inputstream. If it is
     * supported by a file a read-only instance of FileChannel is returned.
     * 
     * @param inputStream
     *            The input stream to convert.
     * @return A readable byte channel.
     */
    public static ReadableByteChannel getChannel(InputStream inputStream) {
        return (inputStream != null) ? Channels.newChannel(inputStream) : null;
    }

    /**
     * Returns a writable byte channel based on a given output stream.
     * 
     * @param outputStream
     *            The output stream.
     * @return A writable byte channel.
     */
    public static WritableByteChannel getChannel(OutputStream outputStream) {
        return (outputStream != null) ? Channels.newChannel(outputStream)
                : null;
    }

    /**
     * Returns a readable byte channel based on the given representation's
     * content and its write(WritableByteChannel) method. Internally, it uses a
     * writer thread and a pipe channel.
     * 
     * @param representation
     *            the representation to get the {@link OutputStream} from.
     * @return A readable byte channel.
     * @throws IOException
     */
    public static ReadableByteChannel getChannel(
            final Representation representation) throws IOException {
        final Pipe pipe = Pipe.open();
        final Application application = Application.getCurrent();

        // Get a thread that will handle the task of continuously
        // writing the representation into the input side of the pipe
        application.getTaskService().execute(new Runnable() {
            public void run() {
                try {
                    final WritableByteChannel wbc = pipe.sink();
                    representation.write(wbc);
                    wbc.close();
                } catch (IOException ioe) {
                    Context.getCurrentLogger().log(Level.FINE,
                            "Error while writing to the piped channel.", ioe);
                }
            }
        });

        return pipe.source();
    }

    /**
     * Returns a reader from an input stream and a character set.
     * 
     * @param stream
     *            The input stream.
     * @param characterSet
     *            The character set. May be null.
     * @return The equivalent reader.
     * @throws UnsupportedEncodingException
     *             if a character set is given, but not supported
     */
    public static Reader getReader(InputStream stream, CharacterSet characterSet)
            throws UnsupportedEncodingException {
        if (characterSet != null) {
            return new InputStreamReader(stream, characterSet.getName());
        }
        return new InputStreamReader(stream);
    }

    /**
     * Returns a reader from a writer representation.Internally, it uses a
     * writer thread and a pipe stream.
     * 
     * @param representation
     *            The representation to read from.
     * @return The character reader.
     * @throws IOException
     */
    public static Reader getReader(final WriterRepresentation representation)
            throws IOException {
        final PipedWriter pipedWriter = new PipedWriter();
        final PipedReader pipedReader = new PipedReader(pipedWriter);
        final Application application = Application.getCurrent();

        // Gets a thread that will handle the task of continuously
        // writing the representation into the input side of the pipe
        application.getTaskService().execute(new Runnable() {
            public void run() {
                try {
                    representation.write(pipedWriter);
                    pipedWriter.close();
                } catch (IOException ioe) {
                    Context.getCurrentLogger().log(Level.FINE,
                            "Error while writing to the piped reader.", ioe);
                }
            }
        });

        return pipedReader;
    }

    /**
     * Returns an input stream based on a given readable byte channel.
     * 
     * @param readableChannel
     *            The readable byte channel.
     * @return An input stream based on a given readable byte channel.
     */
    public static InputStream getStream(ReadableByteChannel readableChannel) {
        InputStream result = null;

        if (readableChannel != null) {
            result = new NbChannelInputStream(readableChannel);
        }

        return result;
    }

    /**
     * Returns an input stream based on a given character reader.
     * 
     * @param reader
     *            The character reader.
     * @param characterSet
     *            The stream character set.
     * @return An input stream based on a given character reader.
     */
    public static InputStream getStream(Reader reader, CharacterSet characterSet) {
        InputStream result = null;

        try {
            result = new ReaderInputStream(reader, characterSet);
        } catch (IOException e) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to create the reader input stream", e);
        }

        return result;
    }

    /**
     * Returns an input stream based on the given representation's content and
     * its write(OutputStream) method. Internally, it uses a writer thread and a
     * pipe stream.
     * 
     * @param representation
     *            the representation to get the {@link OutputStream} from.
     * @return A stream with the representation's content.
     */
    public static InputStream getStream(final Representation representation) {
        if (representation == null) {
            return null;
        }

        final PipeStream pipe = new PipeStream();
        final Application application = Application.getCurrent();

        // Creates a thread that will handle the task of continuously
        // writing the representation into the input side of the pipe
        application.getTaskService().execute(new Runnable() {
            public void run() {
                try {
                    final OutputStream os = pipe.getOutputStream();
                    representation.write(os);
                    os.write(-1);
                    os.close();
                } catch (IOException ioe) {
                    Context.getCurrentLogger().log(Level.FINE,
                            "Error while writing to the piped input stream.",
                            ioe);
                }
            }
        });

        return pipe.getInputStream();
    }

    /**
     * Returns an output stream based on a given writable byte channel.
     * 
     * @param writableChannel
     *            The writable byte channel.
     * @return An output stream based on a given writable byte channel.
     */
    public static OutputStream getStream(WritableByteChannel writableChannel) {
        OutputStream result = null;

        if (writableChannel instanceof SelectableChannel) {
            final SelectableChannel selectableChannel = (SelectableChannel) writableChannel;

            synchronized (selectableChannel.blockingLock()) {
                if (selectableChannel.isBlocking()) {
                    result = Channels.newOutputStream(writableChannel);
                } else {
                    result = new NbChannelOutputStream(writableChannel);
                }
            }
        } else {
            result = new NbChannelOutputStream(writableChannel);
        }

        return result;
    }

    /**
     * Returns an output stream based on a given writer.
     * 
     * @param writer
     *            The writer.
     * @return the output stream of the writer
     */
    public static OutputStream getStream(Writer writer) {
        return new WriterOutputStream(writer);
    }

    /**
     * Release the selection key, working around for bug #6403933.
     * 
     * @param selector
     *            The associated selector.
     * @param selectionKey
     *            The used selection key.
     * @throws IOException
     */
    private static void release(Selector selector, SelectionKey selectionKey)
            throws IOException {
        if (selectionKey != null) {
            // The key you registered on the temporary selector
            selectionKey.cancel();

            if (selector != null) {
                // Flush the cancelled key
                selector.selectNow();
                SelectorFactory.returnSelector(selector);
            }
        }

    }

    /**
     * Converts an input stream to a string.<br>
     * As this method uses the InputstreamReader class, the default character
     * set is used for decoding the input stream.
     * 
     * @see <a href=
     *      "http://java.sun.com/j2se/1.5.0/docs/api/java/io/InputStreamReader.html"
     *      >InputStreamReader class< /a>
     * @see #toString(InputStream, CharacterSet)
     * @param inputStream
     *            The input stream.
     * @return The converted string.
     */
    public static String toString(InputStream inputStream) {
        return toString(inputStream, null);
    }

    /**
     * Converts an input stream to a string using the specified character set
     * for decoding the input stream.
     * 
     * @see <a href=
     *      "http://java.sun.com/j2se/1.5.0/docs/api/java/io/InputStreamReader.html"
     *      >InputStreamReader class< /a>
     * @param inputStream
     *            The input stream.
     * @param characterSet
     *            The character set
     * @return The converted string.
     */
    public static String toString(InputStream inputStream,
            CharacterSet characterSet) {
        String result = null;

        if (inputStream != null) {
            try {
                if (characterSet != null) {
                    result = toString(new InputStreamReader(inputStream,
                            characterSet.getName()));
                } else {
                    result = toString(new InputStreamReader(inputStream));
                }
            } catch (Exception e) {
                // Returns an empty string
            }
        }

        return result;
    }

    /**
     * Converts a reader to a string.
     * 
     * @see <a
     *      href="http://java.sun.com/j2se/1.5.0/docs/api/java/io/InputStreamReader.html">InputStreamReader
     *      class</a>
     * @param reader
     *            The characters reader.
     * @return The converted string.
     */
    public static String toString(Reader reader) {
        String result = null;

        if (reader != null) {
            try {
                final StringBuilder sb = new StringBuilder();
                final BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader
                        : new BufferedReader(reader);
                char[] buffer = new char[8192];
                int charsRead = br.read(buffer);

                while (charsRead != -1) {
                    sb.append(buffer, 0, charsRead);
                    charsRead = br.read(buffer);
                }

                br.close();
                result = sb.toString();
            } catch (Exception e) {
                // Returns an empty string
            }
        }

        return result;
    }

    /**
     * Waits for the given channel to be ready for a specific operation.
     * 
     * @param selectableChannel
     *            The channel to monitor.
     * @param operations
     *            The operations to be ready to do.
     * @throws IOException
     */
    private static void waitForState(SelectableChannel selectableChannel,
            int operations) throws IOException {
        if (selectableChannel != null) {
            Selector selector = null;
            SelectionKey selectionKey = null;
            int selected = 0;

            try {
                selector = SelectorFactory.getSelector();

                while (selected == 0) {
                    selectionKey = selectableChannel.register(selector,
                            operations);
                    selected = selector.select(NIO_TIMEOUT);
                }
            } finally {
                release(selector, selectionKey);
            }
        }
    }

    /**
     * Writes the representation to a byte channel. Optimizes using the file
     * channel transferTo method.
     * 
     * @param fileChannel
     *            The readable file channel.
     * @param writableChannel
     *            A writable byte channel.
     */
    public static void write(FileChannel fileChannel,
            WritableByteChannel writableChannel) throws IOException {
        long position = 0;
        long count = fileChannel.size();
        long written = 0;
        SelectableChannel selectableChannel = null;

        if (writableChannel instanceof SelectableChannel) {
            selectableChannel = (SelectableChannel) writableChannel;
        }

        while (count > 0) {
            waitForState(selectableChannel, SelectionKey.OP_WRITE);
            written = fileChannel.transferTo(position, count, writableChannel);
            position += written;
            count -= written;
        }
    }

    /**
     * Writes an input stream to an output stream. When the reading is done, the
     * input stream is closed.
     * 
     * @param inputStream
     *            The input stream.
     * @param outputStream
     *            The output stream.
     * @throws IOException
     */
    public static void write(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        int bytesRead;
        final byte[] buffer = new byte[4096];
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
    }

    /**
     * Writes an input stream to a random access file. When the reading is done,
     * the input stream is closed.
     * 
     * @param inputStream
     *            The input stream.
     * @param randomAccessFile
     *            The random access file.
     * @throws IOException
     */
    public static void write(InputStream inputStream,
            RandomAccessFile randomAccessFile) throws IOException {
        int bytesRead;
        final byte[] buffer = new byte[2048];
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            randomAccessFile.write(buffer, 0, bytesRead);
        }
        inputStream.close();
    }

    /**
     * Writes a readable channel to a writable channel.
     * 
     * @param readableChannel
     *            The readable channel.
     * @param writableChannel
     *            The writable channel.
     * @throws IOException
     */
    public static void write(ReadableByteChannel readableChannel,
            WritableByteChannel writableChannel) throws IOException {
        if ((readableChannel != null) && (writableChannel != null)) {
            write(new NbChannelInputStream(readableChannel),
                    new NbChannelOutputStream(writableChannel));
        }
    }

    /**
     * Writes characters from a reader to a writer. When the reading is done,
     * the reader is closed.
     * 
     * @param reader
     *            The reader.
     * @param writer
     *            The writer.
     * @throws IOException
     */
    public static void write(Reader reader, Writer writer) throws IOException {
        int charsRead;
        final char[] buffer = new char[2048];
        while ((charsRead = reader.read(buffer)) > 0) {
            writer.write(buffer, 0, charsRead);
        }
        reader.close();
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private ByteUtils() {

    }

}
