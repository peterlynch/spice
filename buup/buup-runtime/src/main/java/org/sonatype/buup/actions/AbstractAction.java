package org.sonatype.buup.actions;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.buup.Buup;

public abstract class AbstractAction
    implements Action
{
    private Logger logger = LoggerFactory.getLogger( getClass() );

    private Buup buup;

    public AbstractAction( Buup buup )
    {
        this.buup = buup;
    }

    // ==

    protected Logger getLogger()
    {
        return logger;
    }

    protected Buup getBuup()
    {
        return buup;
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
