package org.sonatype.appbooter;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;

public interface PlexusAppBooterCustomizer
{
    void customizeContext( Context context );

    void customizeContainerConfiguration( ContainerConfiguration containerConfiguration );

    void customizeContainer( PlexusContainer plexusContainer );
}
