package org.sonatype.plexus.encryptor.cli;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

public abstract class AbstractPlexusEncryptorCliTest
    extends PlexusTestCase
{

    protected static final File DEST_DIR = new File( getBasedir(), "target/output" ).getAbsoluteFile();

    private static final String PUBLIC_KEY =
        new File( getBasedir(), "src/test/resources/UT-public-key.txt" ).getAbsolutePath();

    private static final String PRIVATE_KEY =
        new File( getBasedir(), "src/test/resources/UT-private-key.txt" ).getAbsolutePath();

    private static final File ENCRYPTED_TEXT_SOURCE =
        new File( getBasedir(), "src/test/resources/text.enc" ).getAbsoluteFile();

    private static final String ENCRYPTED_TEXT = new File( DEST_DIR, "text.enc" ).getAbsolutePath();

    private static final File TEXT_SOURCE = new File( getBasedir(), "src/test/resources/text.txt" ).getAbsoluteFile();

    private static final String TEXT = new File( DEST_DIR, "text.txt" ).getAbsolutePath();

    protected OutputStream out;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        out = new OutputStream()
        {

            private StringBuffer buf = new StringBuffer();

            @Override
            public void write( int b )
                throws IOException
            {
                byte[] bytes = new byte[1];
                bytes[0] = (byte) b;
                buf.append( new String( bytes ) );
            }

            @Override
            public String toString()
            {
                String string = buf.toString();
                buf = new StringBuffer();
                return string;
            }
        };

        FileUtils.deleteDirectory( DEST_DIR );
        DEST_DIR.mkdirs();

        FileUtils.copyFileToDirectory( TEXT_SOURCE, DEST_DIR );
        FileUtils.copyFileToDirectory( ENCRYPTED_TEXT_SOURCE, DEST_DIR );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

    }

    public void testNoArgs()
    {
        int code = execute();
        String output = out.toString();
        assertEquals( output, 1, code );
        assertTrue( "Should print usage\n" + output, output.contains( "java -jar plexus-encryptor [options]" ) );
    }

    public void testKeyGeneration()
        throws Exception
    {
        int code = execute( "-g" );
        String output = out.toString();
        assertEquals( output, 0, code );

        File publicKey = new File( DEST_DIR, "public.key" );
        assertTrue( publicKey.exists() );

        File privateKey = new File( DEST_DIR, "private.key" );
        assertTrue( privateKey.exists() );
    }

    public void testKeyGenWithDestinations()
        throws Exception
    {
        File publicKey = new File( DEST_DIR, "gen-public.key" );
        File privateKey = new File( DEST_DIR, "gen-private.key" );

        int code = execute( "-g", "-p", publicKey.getAbsolutePath(), "-k", privateKey.getAbsolutePath() );

        String output = out.toString();
        assertEquals( output, 0, code );

        assertTrue( publicKey.exists() );
        assertTrue( privateKey.exists() );
    }

    public void testDecryptNoSource()
        throws Exception
    {
        int code = execute( "-d", "-k", PRIVATE_KEY );

        String output = out.toString();
        assertEquals( output, 1, code );

        assertTrue( "Should print usage\n" + output, output.contains( "Source file not specified." ) );
    }

    public void testDecrypt()
        throws Exception
    {
        int code = execute( "-d", "-k", PRIVATE_KEY, "-i", ENCRYPTED_TEXT );

        String output = out.toString();
        assertEquals( output, 0, code );

        assertTrue( new File( ENCRYPTED_TEXT + ".dec" ).exists() );
    }

    public void testGenEncDec()
        throws Exception
    {
        int code = execute( "-g" );
        String output = out.toString();
        assertEquals( output, 0, code );

        code = execute( "-e", "-i", TEXT );
        output = out.toString();
        assertEquals( output, 0, code );

        File encFile = new File( TEXT + ".enc" );
        assertTrue( encFile.exists() );

        code = execute( "-d", "-i", encFile.getAbsolutePath() );
        output = out.toString();
        assertEquals( output, 0, code );

        File decFile = new File( encFile.getAbsolutePath() + ".dec" );
        assertTrue( decFile.exists() );
    }

    protected abstract int execute( String... args );

}