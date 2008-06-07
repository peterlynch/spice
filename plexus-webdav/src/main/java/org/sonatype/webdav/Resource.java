package org.sonatype.webdav;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andrew Williams
 */
public interface Resource
{
    public InputStream streamContent( MethodExecutionContext context )
        throws IOException;

    public void setContent( InputStream is )
        throws IOException;

    public Resource copy( MethodExecutionContext context )
        throws ResourceException,
            UnauthorizedException;

    public void remove( MethodExecutionContext context )
        throws ResourceException,
            UnauthorizedException;

    public String getName();

    public void setName( String name );

    public boolean getExists();

    public String getETag( boolean b );

    public String getETag();

    public long getContentLength();

    public long getLastModified();

    public String getLastModifiedHttp();

    public String getMimeType();

    public void setMimeType( String type );

    public long getCreation();
}
