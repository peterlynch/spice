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

package org.restlet.ext.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.XmlRepresentation;
import org.w3c.dom.Document;

/**
 * An XML representation based on JAXB that provides easy translation between
 * XML and JAXB element class trees.
 * 
 * @author Overstock.com
 * @author Jerome Louvel
 * @param <T>
 *            The type to wrap.
 */
public class JaxbRepresentation<T> extends XmlRepresentation {
    /**
     * This is a utility class to assist in marshalling Java content trees into
     * XML. Each {@code marshal} method takes a different target for the XML.
     * This class is a factory that constructs an instance of itself for
     * multiple uses. The created instance is thread safe and is optimized to be
     * used for multiple, possibly concurrent calls.
     */
    private class Marshaller {
        // Use thread identity to preserve safety of access to marshallers.
        private final ThreadLocal<javax.xml.bind.Marshaller> marshaller = new ThreadLocal<javax.xml.bind.Marshaller>() {
            @Override
            protected synchronized javax.xml.bind.Marshaller initialValue() {
                javax.xml.bind.Marshaller m = null;

                try {
                    m = getContext(getPackage()).createMarshaller();
                    m.setProperty("jaxb.formatted.output", isFormattedOutput());

                    if (getCharacterSet() != null) {
                        m.setProperty("jaxb.encoding", getCharacterSet()
                                .getName());
                    }
                } catch (Exception e) {
                    Context.getCurrentLogger().log(Level.WARNING,
                            "Problem creating Marshaller", e);
                    return null;
                }

                return m;
            }
        };

        private final String pkg;

        // This is a factory class.
        private Marshaller() {
            this(null);
        }

        private Marshaller(String pkg) {
            this.pkg = pkg;
        }

        private javax.xml.bind.Marshaller getMarshaller() throws JAXBException {
            final javax.xml.bind.Marshaller m = this.marshaller.get();
            if (m == null) {
                Context.getCurrentLogger().warning(
                        "Unable to locate marshaller.");
                throw new JAXBException("Unable to locate marshaller.");
            }
            return m;
        }

        String getPackage() {
            return this.pkg;
        }

        /**
         * Marshal the content tree rooted at {@code jaxbElement} into an output
         * stream.
         * 
         * @param jaxbElement
         *            The root of the content tree to be marshalled.
         * @param stream
         *            The target output stream write the XML to.
         * @throws JAXBException
         *             If any unexpected problem occurs during marshalling.
         */
        public void marshal(Object jaxbElement, OutputStream stream)
                throws JAXBException {
            getMarshaller().marshal(jaxbElement, stream);
        }

        /**
         * Marshal the content tree rooted at {@code jaxbElement} into a Restlet
         * String representation.
         * 
         * @param jaxbElement
         *            The root of the content tree to be marshalled.
         * @param rep
         *            The target string representation write the XML to.
         * @throws JAXBException
         *             If any unexpected problem occurs during marshalling.
         */
        public void marshal(Object jaxbElement, StringRepresentation rep)
                throws JAXBException {
            final StringWriter writer = new StringWriter();
            marshal(jaxbElement, writer);
            rep.setText(writer.toString());
        }

        /**
         * Marshal the content tree rooted at {@code jaxbElement} into a writer.
         * 
         * @param jaxbElement
         *            The root of the content tree to be marshalled.
         * @param writer
         *            The target writer to write the XML to.
         * @throws JAXBException
         *             If any unexpected problem occurs during marshalling.
         */
        public void marshal(Object jaxbElement, Writer writer)
                throws JAXBException {
            getMarshaller().marshal(jaxbElement, writer);
        }

        /**
         * Sets the validation handler for this marshaller.
         * 
         * @param handler
         *            A validation handler.
         * @throws JAXBException
         *             If an error was encountered while setting the event
         *             handler.
         */
        public void setEventHandler(ValidationEventHandler handler)
                throws JAXBException {
            getMarshaller().setEventHandler(handler);
        }
    }

    /**
     * This is a utility class to assist in unmarshalling XML into a new Java
     * content tree. Each {@code unmarshal} method takes a different source for
     * the XML. This class caches information to improve unmarshalling
     * performance across calls using the same schema (package).
     */
    private class Unmarshaller {
        private final String pkg;

        // Use thread identity to preserve safety of access to unmarshallers.
        private final ThreadLocal<javax.xml.bind.Unmarshaller> unmarshaller = new ThreadLocal<javax.xml.bind.Unmarshaller>() {
            @Override
            protected synchronized javax.xml.bind.Unmarshaller initialValue() {
                javax.xml.bind.Unmarshaller m = null;
                try {
                    m = getContext(getPackage()).createUnmarshaller();
                } catch (Exception e) {
                    Context.getCurrentLogger().log(Level.WARNING,
                            "Problem creating Unmarshaller", e);
                    return null;
                }
                return m;
            }
        };

        // This is a factory class.
        Unmarshaller(String pkg) {
            this.pkg = pkg;
        }

        String getPackage() {
            return this.pkg;
        }

        private javax.xml.bind.Unmarshaller getUnmarshaller()
                throws JAXBException {
            final javax.xml.bind.Unmarshaller m = this.unmarshaller.get();
            if (m == null) {
                Context.getCurrentLogger().warning(
                        "Unable to locate unmarshaller.");
                throw new JAXBException("Unable to locate unmarshaller.");
            }
            return m;
        }

        /**
         * Sets the validation handler for this unmarshaller.
         * 
         * @param handler
         *            A validation handler.
         * @throws JAXBException
         *             If an error was encountered while setting the event
         *             handler.
         */
        public void setEventHandler(ValidationEventHandler handler)
                throws JAXBException {
            getUnmarshaller().setEventHandler(handler);
        }

        /**
         * Unmarshal XML data from the specified input stream and return the
         * resulting Java content tree.
         * 
         * @param stream
         *            The source input stream.
         * @return The newly created root object of the Java content tree.
         * @throws JAXBException
         *             If any unexpected problem occurs during unmarshalling.
         * @throws IOException
         *             If an error occurs accessing the string representation.
         */
        public Object unmarshal(InputStream stream) throws JAXBException {
            return getUnmarshaller().unmarshal(stream);
        }

        /**
         * Unmarshal XML data from the specified reader and return the resulting
         * Java content tree.
         * 
         * @param reader
         *            The source reader.
         * @return The newly created root object of the Java content tree.
         * @throws JAXBException
         *             If any unexpected problem occurs during unmarshalling.
         * @throws IOException
         *             If an error occurs accessing the string representation.
         */
        public Object unmarshal(Reader reader) throws JAXBException {
            return getUnmarshaller().unmarshal(reader);
        }

        /**
         * Unmarshal XML data from the specified Restlet string representation
         * and return the resulting Java content tree.
         * 
         * @param rep
         *            The source string representation.
         * @return The newly created root object of the Java content tree.
         * @throws JAXBException
         *             If any unexpected problem occurs during unmarshalling.
         * @throws IOException
         *             If an error occurs accessing the string representation.
         */
        public Object unmarshal(StringRepresentation rep) throws JAXBException,
                IOException {
            return getUnmarshaller().unmarshal(rep.getStream());
        }
    }

    /** Improves performance by caching contexts which are expensive to create. */
    private final static Map<String, JAXBContext> contexts = new TreeMap<String, JAXBContext>();

    /**
     * Returns the JAXB context.
     * 
     * @return The JAXB context.
     * @throws JAXBException
     */
    private static synchronized JAXBContext getContext(String contextPath)
            throws JAXBException {
        // Contexts are thread-safe so reuse those.
        JAXBContext result = contexts.get(contextPath);

        if (result == null) {
            result = JAXBContext.newInstance(contextPath);
            contexts.put(contextPath, result);
        }

        return result;
    }

    /**
     * The list of Java package names that contain schema derived class and/or
     * Java to schema (JAXB-annotated) mapped classes.
     */
    private volatile String contextPath;

    /**
     * Indicates if the resulting XML data should be formatted with line breaks
     * and indentation. Defaults to false.
     */
    private volatile boolean formattedOutput;

    /** The wrapped Java object. */
    private volatile T object;

    /** The JAXB validation event handler. */
    private volatile ValidationEventHandler validationEventHandler;

    /** The source XML representation. */
    private volatile Representation xmlRepresentation;

    /**
     * Creates a JAXB representation from an existing JAXB content tree.
     * 
     * @param mediaType
     *            The representation's media type.
     * @param object
     *            The Java object.
     */
    public JaxbRepresentation(MediaType mediaType, T object) {
        super(mediaType);
        this.object = object;
        this.contextPath = (object != null) ? object.getClass().getPackage()
                .getName() : null;
        this.validationEventHandler = null;
        this.xmlRepresentation = null;
    }

    /**
     * Creates a new JAXB representation, converting the input XML into a Java
     * content tree. The XML is validated.
     * 
     * @param xmlRepresentation
     *            The XML wrapped in a representation.
     * @param type
     *            The type to convert to.
     * 
     * @throws JAXBException
     *             If the incoming XML does not validate against the schema.
     * @throws IOException
     *             If unmarshalling XML fails.
     */
    public JaxbRepresentation(Representation xmlRepresentation, Class<T> type) {
        this(xmlRepresentation, type.getPackage().getName(), null);
    }

    /**
     * Creates a new JAXB representation, converting the input XML into a Java
     * content tree. The XML is validated.
     * 
     * @param xmlRepresentation
     *            The XML wrapped in a representation.
     * @param type
     *            The type to convert to.
     * @param validationHandler
     *            A handler for dealing with validation failures.
     * 
     * @throws JAXBException
     *             If the incoming XML does not validate against the schema.
     * @throws IOException
     *             If unmarshalling XML fails.
     */
    public JaxbRepresentation(Representation xmlRepresentation, Class<T> type,
            ValidationEventHandler validationHandler) {
        this(xmlRepresentation, type.getPackage().getName(), validationHandler);
    }

    /**
     * Creates a new JAXB representation, converting the input XML into a Java
     * content tree. The XML is validated.
     * 
     * @param xmlRepresentation
     *            The XML wrapped in a representation.
     * @param contextPath
     *            The list of Java package names for JAXB.
     * 
     * @throws JAXBException
     *             If the incoming XML does not validate against the schema.
     * @throws IOException
     *             If unmarshalling XML fails.
     */
    public JaxbRepresentation(Representation xmlRepresentation,
            String contextPath) {
        this(xmlRepresentation, contextPath, null);
    }

    /**
     * Creates a new JAXB representation, converting the input XML into a Java
     * content tree. The XML is validated.
     * 
     * @param xmlRepresentation
     *            The XML wrapped in a representation.
     * @param contextPath
     *            The list of Java package names for JAXB.
     * @param validationHandler
     *            A handler for dealing with validation failures.
     * 
     * @throws JAXBException
     *             If the incoming XML does not validate against the schema.
     * @throws IOException
     *             If unmarshalling XML fails.
     */
    public JaxbRepresentation(Representation xmlRepresentation,
            String contextPath, ValidationEventHandler validationHandler) {
        super(xmlRepresentation.getMediaType());
        this.contextPath = contextPath;
        this.object = null;
        this.validationEventHandler = validationHandler;
        this.xmlRepresentation = xmlRepresentation;

    }

    /**
     * Creates a JAXB representation from an existing JAXB content tree with
     * {@link MediaType#APPLICATION_XML}.
     * 
     * @param object
     *            The Java object.
     */
    public JaxbRepresentation(T object) {
        this(MediaType.APPLICATION_XML, object);
    }

    @Override
    public Object evaluate(String expression, QName returnType)
            throws Exception {
        Object result = null;
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(this);

        final Document xmlDocument = getDocumentBuilder().parse(
                this.xmlRepresentation.getStream());

        if (xmlDocument != null) {
            result = xpath.evaluate(expression, xmlDocument, returnType);
        } else {
            throw new Exception(
                    "Unable to obtain a DOM document for the XML representation. "
                            + "XPath evaluation cancelled.");
        }

        return result;
    }

    /**
     * Returns the JAXB context.
     * 
     * @return The JAXB context.
     * @throws JAXBException
     */
    private JAXBContext getContext() throws JAXBException {
        return getContext(getContextPath());
    }

    /**
     * Returns the list of Java package names that contain schema derived class
     * and/or Java to schema (JAXB-annotated) mapped classes
     * 
     * @return The list of Java package names.
     */
    public String getContextPath() {
        return this.contextPath;
    }

    /**
     * Returns the wrapped Java object.
     * 
     * @return The wrapped Java object.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public T getObject() throws IOException {
        if ((this.object == null) && (this.xmlRepresentation != null)) {
            // Try to unmarshal the wrapped XML representation
            final Unmarshaller u = new Unmarshaller(this.contextPath);
            if (getValidationEventHandler() != null) {
                try {
                    u.setEventHandler(getValidationEventHandler());
                } catch (JAXBException e) {
                    Context.getCurrentLogger().log(Level.WARNING,
                            "Unable to set the event handler", e);
                    throw new IOException("Unable to set the event handler."
                            + e.getMessage());
                }
            }

            try {
                this.object = (T) u.unmarshal(this.xmlRepresentation
                        .getStream());
            } catch (JAXBException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Unable to unmarshal the XML representation", e);
                throw new IOException(
                        "Unable to unmarshal the XML representation."
                                + e.getMessage());
            }
        }
        return this.object;
    }

    /**
     * Returns a JAXB SAX source.
     * 
     * @return A JAXB SAX source.
     */
    @Override
    public SAXSource getSaxSource() throws IOException {
        try {
            return new JAXBSource(getContext(), getObject());
        } catch (JAXBException e) {
            throw new IOException(
                    "JAXBException while creating the JAXBSource: "
                            + e.getMessage());
        }
    }

    /**
     * Returns the optional validation event handler.
     * 
     * @return The optional validation event handler.
     */
    public ValidationEventHandler getValidationEventHandler() {
        return this.validationEventHandler;
    }

    /**
     * Indicates if the resulting XML data should be formatted with line breaks
     * and indentation. Defaults to false.
     * 
     * @return the formattedOutput
     */
    public boolean isFormattedOutput() {
        return this.formattedOutput;
    }

    /**
     * Sets the list of Java package names that contain schema derived class
     * and/or Java to schema (JAXB-annotated) mapped classes.
     * 
     * @param contextPath
     *            The JAXB context path.
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Indicates if the resulting XML data should be formatted with line breaks
     * and indentation.
     * 
     * @param formattedOutput
     *            True if the resulting XML data should be formatted.
     */
    public void setFormattedOutput(boolean formattedOutput) {
        this.formattedOutput = formattedOutput;
    }

    /**
     * Sets the wrapped Java object.
     * 
     * @param object
     *            The Java object to set.
     */
    public void setObject(T object) {
        this.object = object;
    }

    /**
     * Writes the representation to a byte stream.
     * 
     * @param outputStream
     *            The output stream.
     * 
     * @throws IOException
     *             If any error occurs attempting to write the stream.
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            new Marshaller(this.contextPath).marshal(getObject(), outputStream);
        } catch (JAXBException e) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "JAXB marshalling error caught.", e);

            // Maybe the tree represents a failure, try that.
            try {
                new Marshaller("failure").marshal(getObject(), outputStream);
            } catch (JAXBException e2) {
                // We don't know what package this tree is from.
                throw new IOException(e.getMessage());
            }
        }
    }

}
