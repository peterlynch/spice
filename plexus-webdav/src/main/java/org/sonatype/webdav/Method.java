package org.sonatype.webdav;

import org.sonatype.webdav.security.Authorization;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The interface for all webdav methods
 * 
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface Method
{
    public void setAuthorization( Authorization authz );

    public void setResourceCollection( ResourceCollection collection );

    void setSecret( String secret );

    boolean isReadOnly();

    void setReadOnly( boolean readOnly );

    int getDebug();

    void setDebug( int debug );

    boolean getListings();

    void setListings( boolean listings );

    void setFileEncoding( String fileEncoding );

    void execute( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse res )
        throws IOException, UnauthorizedException;
}
