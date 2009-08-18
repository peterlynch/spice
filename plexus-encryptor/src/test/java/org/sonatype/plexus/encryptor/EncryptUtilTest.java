package org.sonatype.plexus.encryptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.plexus.encryptor.PlexusEncryptor;
import org.sonatype.plexus.encryptor.RsaAesPlexusEncryptor;

public class EncryptUtilTest
    extends PlexusTestCase
{

    private static final String ENCRYPTED_TEXT;

    private static final String PUBLIC_KEY;

    private static final String PRIVATE_KEY;

    private static final String BIG_TEXT_TO_ENCRYPT;

    static
    {
        try
        {
            ENCRYPTED_TEXT = IOUtil.toString( EncryptUtilTest.class.getResourceAsStream( "/text.enc" ) );
            PUBLIC_KEY = IOUtil.toString( EncryptUtilTest.class.getResourceAsStream( "/UT-public-key.txt" ) );
            PRIVATE_KEY = IOUtil.toString( EncryptUtilTest.class.getResourceAsStream( "/UT-private-key.txt" ) );
            BIG_TEXT_TO_ENCRYPT = IOUtil.toString( EncryptUtilTest.class.getResourceAsStream( "/text.txt" ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    private RsaAesPlexusEncryptor encryptor;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        encryptor = (RsaAesPlexusEncryptor) lookup( PlexusEncryptor.class, "rsa-aes" );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        encryptor = null;

        super.tearDown();
    }

    public void testGenerateKeys()
        throws GeneralSecurityException, IOException
    {
        ByteArrayOutputStream publicKeyOut = new ByteArrayOutputStream();
        ByteArrayOutputStream privateKeyOut = new ByteArrayOutputStream();

        encryptor.generateKeys( publicKeyOut, privateKeyOut );

        byte[] publicBytes = publicKeyOut.toByteArray();
        byte[] privateBytes = privateKeyOut.toByteArray();
        assertFalse( publicBytes.length == 0 );
        assertFalse( privateBytes.length == 0 );
        assertTrue( privateBytes.length > publicBytes.length );

        assertFalse( new String( publicBytes ).equals( PUBLIC_KEY ) );
        assertFalse( new String( privateBytes ).equals( PRIVATE_KEY ) );

        System.out.println( "Public key:\n" + new String( publicBytes ) );
        System.out.println( "Private key:\n" + new String( privateBytes ) );

        final ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        encryptor.encrypt( new ByteArrayInputStream( "Simple decryption test!".getBytes() ), encryptedOut,
                           new ByteArrayInputStream( publicBytes ) );

        String encryptedText = new String( encryptedOut.toByteArray() );
        System.out.println( "Encrypted text:\n" + encryptedText );
    }

    public void testKeyRead()
        throws Exception
    {
        PublicKey publicKey =
            encryptor.readPublicKey( new ByteArrayInputStream( EncryptUtilTest.PUBLIC_KEY.getBytes() ) );
        assertNotNull( publicKey );

        PrivateKey privateKey =
            encryptor.readPrivateKey( new ByteArrayInputStream( EncryptUtilTest.PRIVATE_KEY.getBytes() ) );
        assertNotNull( privateKey );
    }

    public void donttestEncrypt()
        throws Exception
    {
        final ByteArrayOutputStream plainOutput = new ByteArrayOutputStream();
        encryptor.encrypt( new ByteArrayInputStream( "Simple decryption test!".getBytes() ), plainOutput,
                           new ByteArrayInputStream( PUBLIC_KEY.getBytes() ) );

        String encryptedText = new String( plainOutput.toByteArray() );
        System.out.println( encryptedText );
    }

    public void testDecrypt()
        throws Exception
    {
        final ByteArrayOutputStream plainOutput = new ByteArrayOutputStream();
        encryptor.decrypt( new ByteArrayInputStream( ENCRYPTED_TEXT.getBytes() ), plainOutput,
                           new ByteArrayInputStream( PRIVATE_KEY.getBytes() ) );

        String decryptedText = new String( plainOutput.toByteArray() );
        assertEquals( "Simple decryption test!", decryptedText );
    }

    public void testEncDec()
        throws Exception
    {
        String textToEncrypt = "This is a simple text to be encrypted!!!";

        final ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        encryptor.encrypt( new ByteArrayInputStream( textToEncrypt.getBytes() ), encryptedOut,
                           new ByteArrayInputStream( PUBLIC_KEY.getBytes() ) );

        String encryptedText = new String( encryptedOut.toByteArray() );
        System.out.println( "Enc text" + encryptedText );
        assertFalse( textToEncrypt.equals( encryptedText ) );

        final ByteArrayOutputStream plainOutput = new ByteArrayOutputStream();
        encryptor.decrypt( new ByteArrayInputStream( encryptedOut.toByteArray() ), plainOutput,
                           new ByteArrayInputStream( PRIVATE_KEY.getBytes() ) );

        String decryptedText = new String( plainOutput.toByteArray() );
        assertEquals( textToEncrypt, decryptedText );
    }

    public void testEncryptBigText()
        throws Exception
    {
        final ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        encryptor.encrypt( new ByteArrayInputStream( BIG_TEXT_TO_ENCRYPT.getBytes() ), encryptedOut,
                           new ByteArrayInputStream( PUBLIC_KEY.getBytes() ) );

        final ByteArrayOutputStream plainOutput = new ByteArrayOutputStream();
        encryptor.decrypt( new ByteArrayInputStream( encryptedOut.toByteArray() ), plainOutput,
                           new ByteArrayInputStream( PRIVATE_KEY.getBytes() ) );

        assertEquals( BIG_TEXT_TO_ENCRYPT, new String( plainOutput.toByteArray() ) );
    }

    public void testEncryptZip()
        throws Exception
    {
        byte[] zip = IOUtil.toByteArray( getClass().getResourceAsStream( "/compressed.zip" ) );
        final ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        encryptor.encrypt( new ByteArrayInputStream( zip ), encryptedOut,
                           new ByteArrayInputStream( PUBLIC_KEY.getBytes() ) );

        final ByteArrayOutputStream plainOutput = new ByteArrayOutputStream();
        encryptor.decrypt( new ByteArrayInputStream( encryptedOut.toByteArray() ), plainOutput,
                           new ByteArrayInputStream( PRIVATE_KEY.getBytes() ) );

        assertTrue( Arrays.equals( zip, plainOutput.toByteArray() ) );
    }

}
