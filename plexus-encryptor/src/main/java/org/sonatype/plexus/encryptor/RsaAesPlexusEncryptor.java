package org.sonatype.plexus.encryptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;

@Component( role = PlexusEncryptor.class, hint = "rsa-aes" )
public class RsaAesPlexusEncryptor
    extends AbstractLogEnabled
    implements PlexusEncryptor
{

    private static final int KEY_SIZE = 128;

    public void generateKeys( File publicKey, File privateKey )
        throws GeneralSecurityException, IOException
    {
        OutputStream publicKeyOut = null;
        OutputStream privateKeyOut = null;

        try
        {
            publicKey.getParentFile().mkdirs();
            privateKey.getParentFile().mkdirs();

            publicKeyOut = new FileOutputStream( publicKey );
            privateKeyOut = new FileOutputStream( privateKey );

            generateKeys( publicKeyOut, privateKeyOut );
        }
        finally
        {
            IOUtil.close( publicKeyOut );
            IOUtil.close( privateKeyOut );
        }
    }

    public void generateKeys( OutputStream publicKeyOut, OutputStream privateKeyOut )
        throws GeneralSecurityException, IOException
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance( "RSA" );

        SecureRandom random = SecureRandom.getInstance( "SHA1PRNG" );
        generator.initialize( KEY_SIZE * 8, random );

        KeyPair keyPair = generator.generateKeyPair();

        OutputStream privateOut = new Base64OutputStream( privateKeyOut );
        PrivateKey privateKey = keyPair.getPrivate();
        privateOut.write( privateKey.getEncoded() );
        IOUtil.close( privateOut );

        OutputStream publicOut = new Base64OutputStream( publicKeyOut );
        PublicKey publicKey = keyPair.getPublic();
        publicOut.write( publicKey.getEncoded() );
        IOUtil.close( publicOut );
    }

    protected PublicKey readPublicKey( InputStream keyInput )
        throws IOException, GeneralSecurityException
    {
        InputStream input = new Base64InputStream( keyInput );
        byte[] encKey = IOUtil.toByteArray( input );
        IOUtil.close( input );

        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec( encKey );
        KeyFactory keyFactory = KeyFactory.getInstance( "RSA" );
        PublicKey pubKey = keyFactory.generatePublic( pubKeySpec );

        return pubKey;
    }

    protected PrivateKey readPrivateKey( InputStream keyInput )
        throws IOException, GeneralSecurityException
    {
        InputStream input = new Base64InputStream( keyInput );
        byte[] encKey = IOUtil.toByteArray( input );
        IOUtil.close( input );

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec( encKey );
        KeyFactory keyFactory = KeyFactory.getInstance( "RSA" );
        PrivateKey privateKey = keyFactory.generatePrivate( privateKeySpec );

        return privateKey;
    }

    public void encrypt( File source, File destination, File publicKey )
        throws IOException, GeneralSecurityException
    {
        InputStream publicKeyIn = null;
        try
        {
            publicKeyIn = new FileInputStream( publicKey );

            encrypt( source, destination, publicKeyIn );
        }
        finally
        {
            IOUtil.close( publicKeyIn );
        }
    }

    public void encrypt( File source, File destination, InputStream publicKey )
        throws IOException, GeneralSecurityException
    {
        InputStream plainInput = null;
        OutputStream encryptedOutput = null;
        try
        {
            plainInput = new FileInputStream( source );
            encryptedOutput = new FileOutputStream( destination );
            encrypt( plainInput, encryptedOutput, publicKey );
        }
        finally
        {
            IOUtil.close( plainInput );
            IOUtil.close( encryptedOutput );

        }
    }

    public void encrypt( InputStream plainInput, OutputStream encryptedOutput, InputStream publickKey )
        throws IOException, GeneralSecurityException
    {
        PublicKey key = readPublicKey( publickKey );
        encrypt( plainInput, encryptedOutput, key );
    }

    public void encrypt( InputStream plainInput, OutputStream encryptedOutput, PublicKey key )
        throws IOException, GeneralSecurityException
    {
        KeyGenerator kgen = KeyGenerator.getInstance( "AES" );
        kgen.init( KEY_SIZE );

        SecretKey aesKey = kgen.generateKey();

        byte[] data = IOUtil.toByteArray( plainInput );
        byte[] encryptedData = getCipher( "AES", aesKey, Cipher.ENCRYPT_MODE ).doFinal( data );

        byte[] raw = aesKey.getEncoded();
        byte[] encryptedKey = getCipher( "RSA/ECB/PKCS1Padding", key, javax.crypto.Cipher.ENCRYPT_MODE ).doFinal( raw );

        // useful when debugging but can't be left uncommented due to NEXUS-2530
        // if ( getLogger().isDebugEnabled() )
        // {
        // getLogger().debug( "before encrypt: " + new String( Base64.encodeBase64( raw ) ) );
        // getLogger().debug( "Encrypted key: " + new String( Base64.encodeBase64( encryptedKey ) ) );
        // getLogger().debug( "Encrypted data: " + new String( Base64.encodeBase64( encryptedData ) ) );
        // }

        Base64OutputStream output = new Base64OutputStream( encryptedOutput );
        IOUtil.copy( encryptedKey, output );
        IOUtil.copy( encryptedData, output );
        output.close();
        encryptedOutput.flush();
    }

    public void decrypt( File source, File destination, File privateKey )
        throws IOException, GeneralSecurityException
    {
        InputStream encryptedInput = null;
        OutputStream plainOutput = null;
        InputStream secretKey = null;
        try
        {
            encryptedInput = new FileInputStream( source );
            plainOutput = new FileOutputStream( destination );
            secretKey = new FileInputStream( privateKey );

            decrypt( encryptedInput, plainOutput, secretKey );
        }
        finally
        {
            IOUtil.close( encryptedInput );
            IOUtil.close( plainOutput );
            IOUtil.close( secretKey );
        }
    }

    public void decrypt( InputStream encryptedInput, OutputStream plainOutput, InputStream secretKey )
        throws IOException, GeneralSecurityException
    {
        PrivateKey key = readPrivateKey( secretKey );
        decrypt( encryptedInput, plainOutput, key );
    }

    public void decrypt( InputStream encryptedInput, OutputStream plainOutput, PrivateKey key )
        throws IOException, GeneralSecurityException
    {
        byte[] encryptedKey = new byte[KEY_SIZE];
        Base64InputStream input = new Base64InputStream( encryptedInput );
        input.read( encryptedKey );

        byte[] encryptedData = IOUtil.toByteArray( input );
        IOUtil.close( input );

        byte[] raw = getCipher( "RSA/ECB/PKCS1Padding", key, javax.crypto.Cipher.DECRYPT_MODE ).doFinal( encryptedKey );

        // useful when debugging but can't be left uncommented due to NEXUS-2530
        // if ( getLogger().isDebugEnabled() )
        // {
        // getLogger().debug( "enc key: " + new String( Base64.encodeBase64( encryptedKey ) ) );
        // getLogger().debug( "enc data: " + new String( Base64.encodeBase64( encryptedData ) ) );
        // getLogger().debug( "after decrypt: " + new String( Base64.encodeBase64( raw ) ) );
        // }

        SecretKeySpec aesKey = new SecretKeySpec( raw, "AES" );

        byte[] data = getCipher( "AES", aesKey, Cipher.DECRYPT_MODE ).doFinal( encryptedData );

        IOUtil.copy( data, plainOutput );
        plainOutput.flush();
    }

    private Cipher getCipher( String algorithm, Key key, int cipherMode )
        throws GeneralSecurityException
    {
        Cipher cipher = Cipher.getInstance( algorithm );
        cipher.init( cipherMode, key );

        return cipher;
    }

}
