package org.sonatype.plexus.appevents;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

public abstract class AbstractApplicationEventListener
    extends AbstractLogEnabled
    implements EventListener, Initializable, Disposable
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    public void initialize()
        throws InitializationException
    {
        applicationEventMulticaster.addEventListener( this );
    }

    public void dispose()
    {
        applicationEventMulticaster.removeEventListener( this );
    }
}
