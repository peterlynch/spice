package org.sonatype.appbooter;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.sonatype.appcontext.AppContext;

/**
 * A "noop" abstract implementation of PlexusAppBooterCustomizer. This is useful to extend it, and override methods that
 * are really needed to do something.
 * 
 * @author cstamas
 */
public abstract class AbstractPlexusAppBooterCustomizer
    implements PlexusAppBooterCustomizer
{

    public void customizeContext( final PlexusAppBooter appBooter, final AppContext context )
    {
        // noop
    }

    public void customizeContainerConfiguration( final PlexusAppBooter appBooter,
                                                 final ContainerConfiguration containerConfiguration )
    {
        // noop
    }

    public void customizeContainer( final PlexusAppBooter appBooter, final PlexusContainer plexusContainer )
    {
        // noop
    }

}
