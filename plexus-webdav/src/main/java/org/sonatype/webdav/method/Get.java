package org.sonatype.webdav.method;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.UnauthorizedException;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Webdav GET method.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="get"
 * @since 1.0
 */
public class Get
    extends AbstractMethod
{

    public void execute( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse res )
        throws IOException,
            UnauthorizedException
    {
        if ( !authorizeRead( context.getUser(), res ) )
        {
            return;
        }

        // Serve the requested resource, including the data content
        try
        {
            serveResource( context, req, res, true );
        }
        catch ( IOException ex )
        {
            // we probably have this check somewhere else too.
            if ( ex.getMessage() != null && ex.getMessage().indexOf( "Broken pipe" ) >= 0 )
            {
                // ignore it.
            }
            throw ex;
        }
    }
}
