package org.sonatype.webdav.method;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.WebdavStatus;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Webdav UNLOCK method.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="unlock"
 * @since 1.0
 */
public class Unlock
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

        String path = getRelativePath( req );

        String lockTokenHeader = req.getHeader( "Lock-Token" );
        if ( lockTokenHeader == null )
        {
            lockTokenHeader = "";
        }

        // Checking resource locks

        LockInfo lock = (LockInfo) resourceLocks.get( path );
        Iterator iter = null;
        if ( lock != null )
        {
            // At least one of the tokens of the locks must have been given
            iter = lock.tokens.iterator();
            while ( iter.hasNext() )
            {
                String token = (String) iter.next();
                if ( lockTokenHeader.indexOf( token ) != -1 )
                {
                    iter.remove();
                }
            }

            if ( lock.tokens.isEmpty() )
            {
                resourceLocks.remove( path );
                // Removing any lock-null resource which would be present
                lockNullResources.remove( path );
            }

        }

        // Checking inheritable collection locks
        iter = collectionLocks.iterator();
        while ( iter.hasNext() )
        {
            lock = (LockInfo) iter.next();
            if ( path.equals( lock.path ) )
            {
                Iterator tokenIter = lock.tokens.iterator();
                while ( tokenIter.hasNext() )
                {
                    String token = (String) tokenIter.next();
                    if ( lockTokenHeader.indexOf( token ) != -1 )
                    {
                        tokenIter.remove();
                        break;
                    }
                }

                if ( lock.tokens.isEmpty() )
                {
                    iter.remove();
                    // Removing any lock-null resource which would be present
                    lockNullResources.remove( path );
                }

            }
        }

        res.setStatus( WebdavStatus.SC_NO_CONTENT );
    }
}
