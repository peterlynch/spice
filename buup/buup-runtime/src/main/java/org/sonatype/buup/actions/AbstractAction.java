package org.sonatype.buup.actions;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAction
    implements Action
{
    private Logger logger = LoggerFactory.getLogger( getClass() );

    // ==

    protected Logger getLogger()
    {
        return logger;
    }

    protected IOException ioexception( String message )
    {
        return ioexception( message, null );
    }

    protected IOException ioexception( String message, Throwable cause )
    {
        IOException result = new IOException( message );

        if ( cause != null )
        {
            result.initCause( cause );
        }

        return result;
    }
}
