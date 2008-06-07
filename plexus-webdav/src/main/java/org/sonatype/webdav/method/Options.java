package org.sonatype.webdav.method;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.UnauthorizedException;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Webdav OPTIONS method.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="options"
 * @since 1.0
 */
public class Options
    extends AbstractWebdavMethod
{
    public void execute( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse res )
        throws IOException, UnauthorizedException
    {
        if ( !authorizeRead( context.getUser(), res ) )
        {
            return;
        }

        res.addHeader( "DAV", "1,2" );

        StringBuffer methodsAllowed = determineMethodsAllowed( context, resourceCollection, req );

        res.addHeader( "Allow", methodsAllowed.toString() );
        res.addHeader( "MS-Author-Via", "DAV" );
    }
}
