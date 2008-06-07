package org.sonatype.webdav.method;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.UnauthorizedException;
import org.sonatype.webdav.WebdavStatus;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Webdav MOVE method.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="move"
 * @since 1.0
 */
public class Move
    extends Copy
{

    public void execute( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse res )
        throws IOException, UnauthorizedException
    {
        if ( !authorizeWrite( context.getUser(), res ) )
        {
            return;
        }

        if ( isReadOnly() )
        {
            res.sendError( WebdavStatus.SC_FORBIDDEN );
            return;
        }

        if ( isLocked( req ) )
        {
            res.sendError( WebdavStatus.SC_LOCKED );
            return;
        }

        String path = getRelativePath( req );

        if ( copyResource( context, req, res ) )
        {
            deleteResource( context, path, req, res, false );
        }
    }
}
