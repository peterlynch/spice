package org.sonatype.buup.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAction
    implements Action
{
    private Logger logger = LoggerFactory.getLogger( getClass() );

    protected Logger getLogger()
    {
        return logger;
    }
}
