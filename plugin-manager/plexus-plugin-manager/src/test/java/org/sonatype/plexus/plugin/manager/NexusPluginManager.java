package org.sonatype.plexus.plugin.manager;

import org.codehaus.plexus.component.annotations.Component;

@Component( role=PlexusPluginManager.class, hint="nexus")
public class NexusPluginManager
    extends DefaultPlexusPluginManager
{
}
