package org.sonatype.webdav.method;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.WebdavStatus;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by IntelliJ IDEA.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="proppatch"
 * @since 1.0
 */
public class PropPatch
    extends AbstractWebdavMethod
{
    public void execute( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse res )
        throws IOException
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

        res.sendError( HttpServletResponse.SC_NOT_IMPLEMENTED );
    }
}
