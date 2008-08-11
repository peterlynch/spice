package org.sonatype.plexus.classworlds.io;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.codehaus.plexus.logging.Logger;

public class VelocityLogChute
    implements LogChute
{
    
    private static Logger plxLogger;
    
    public static void setPlexusLogger( Logger logger )
    {
        plxLogger = logger;
    }
    
    public static boolean hasPlexusLogger()
    {
        return plxLogger != null;
    }

    public void init( RuntimeServices rs )
        throws Exception
    {
    }

    public boolean isLevelEnabled( int level )
    {
        checkPlexusLogger();
        switch( level )
        {
            case( LogChute.DEBUG_ID ):
            {
                return plxLogger.isDebugEnabled();
            }
            case( LogChute.ERROR_ID ):
            {
                return plxLogger.isErrorEnabled();
            }
            case( LogChute.TRACE_ID ):
            {
                return plxLogger.isDebugEnabled();
            }
            case( LogChute.WARN_ID ):
            {
                return plxLogger.isWarnEnabled();
            }
            default:
            {
                return plxLogger.isInfoEnabled();
            }
        }
    }

    private void checkPlexusLogger()
    {
        if ( plxLogger == null )
        {
            throw new IllegalStateException( "You must call the static method: '" + getClass().getName() + ".setPlexusLogger( logger )' before using this LogChute implementation." );
        }
    }

    public void log( int level, String message )
    {
        log( level, message, null );
    }

    public void log( int level, String message, Throwable cause )
    {
        checkPlexusLogger();
        switch( level )
        {
            case( LogChute.DEBUG_ID ):
            case( LogChute.TRACE_ID ):
            {
                if ( cause == null )
                {
                    plxLogger.debug( message );
                }
                else
                {
                    plxLogger.debug( message, cause );
                }
                break;
            }
            case( LogChute.ERROR_ID ):
            {
                if ( cause == null )
                {
                    plxLogger.error( message );
                }
                else
                {
                    plxLogger.error( message, cause );
                }
                break;
            }
            case( LogChute.WARN_ID ):
            {
                if ( cause == null )
                {
                    plxLogger.warn( message );
                }
                else
                {
                    plxLogger.warn( message, cause );
                }
                break;
            }
            default:
            {
                if ( cause == null )
                {
                    plxLogger.info( message );
                }
                else
                {
                    plxLogger.info( message, cause );
                }
                break;
            }
        }
    }

}
