package org.sonatype.jsecurity.realms.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

/**
 * A util class to calculate various digests on Strings. Usaful for some simple password management.
 * 
 * @author cstamas
 */
public class StringDigester
{

    /**
     * Calculates a digest for a String user the requested algorithm.
     * 
     * @param alg
     * @param content
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String getDigest( String alg, String content )
        throws NoSuchAlgorithmException
    {
        String result = null;

        try
        {
            InputStream fis = new ByteArrayInputStream( content.getBytes( "UTF-8" ) );

            try
            {
                byte[] buffer = new byte[1024];

                MessageDigest md = MessageDigest.getInstance( alg );

                int numRead;

                do
                {
                    numRead = fis.read( buffer );
                    if ( numRead > 0 )
                    {
                        md.update( buffer, 0, numRead );
                    }
                }
                while ( numRead != -1 );

                result = new String( Hex.encodeHex( md.digest() ) );
            }
            finally
            {
                fis.close();
            }
        }
        catch ( IOException e )
        {
            // hrm
            result = null;
        }

        return result;
    }

    /**
     * Calculates a SHA1 digest for a string.
     * 
     * @param content
     * @return
     */
    public static String getSha1Digest( String content )
    {
        try
        {
            return getDigest( "SHA1", content );
        }
        catch ( NoSuchAlgorithmException e )
        {
            // will not happen
            return null;
        }
    }

    /**
     * Calculates MD5 digest for a string.
     * 
     * @param content
     * @return
     */
    public static String getMd5Digest( String content )
    {
        try
        {
            return getDigest( "MD5", content );
        }
        catch ( NoSuchAlgorithmException e )
        {
            // will not happen
            return null;
        }
    }

}
