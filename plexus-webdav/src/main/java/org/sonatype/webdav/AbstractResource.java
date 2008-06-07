package org.sonatype.webdav;

import org.sonatype.webdav.util.RequestUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;


/**
 * @author Jason van Zyl
 * @author Andrew Williams
 */
public abstract class AbstractResource
    implements Resource
{
    private String mime = null;

    public static void copy( final InputStream input, final OutputStream output, final int bufferSize )
        throws IOException
    {
        if ( input == null )
        {
            return;
        }

        final byte[] buffer = new byte[bufferSize];

        int n;

        while ( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }

        output.flush();

        input.close();
        output.close();
    }

    public String getETag( boolean b )
    {
        return null;
    }

    public String getETag()
    {
        return null;
    }

    public String getLastModifiedHttp()
    {
        return RequestUtil.formatHttpDate( new Date( getLastModified() ) );
    }

    public String getMimeType()
    {
        return mime;
    }

    public void setMimeType( String type )
    {
        this.mime = type;
    }
}
