package org.sonatype.plexus.encryptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface PlexusEncryptor
{

    void generateKeys( File publicKey, File privateKey )
        throws GeneralSecurityException, IOException;

    void generateKeys( OutputStream publicKeyOut, OutputStream privateKeyOut )
        throws GeneralSecurityException, IOException;

    void encrypt( File source, File destination, File publicKey )
        throws IOException, GeneralSecurityException;

    void encrypt( InputStream plainInput, OutputStream encryptedOutput, InputStream publickKey )
        throws IOException, GeneralSecurityException;

    void encrypt( InputStream plainInput, OutputStream encryptedOutput, PublicKey key )
        throws IOException, GeneralSecurityException;

    void decrypt( File source, File destination, File privateKey )
        throws IOException, GeneralSecurityException;

    void decrypt( InputStream encryptedInput, OutputStream plainOutput, InputStream secretKey )
        throws IOException, GeneralSecurityException;

    void decrypt( InputStream encryptedInput, OutputStream plainOutput, PrivateKey key )
        throws IOException, GeneralSecurityException;

    void encrypt( File problemReportBundle, File encryptedZip, InputStream publicKey )
        throws IOException, GeneralSecurityException;

}
