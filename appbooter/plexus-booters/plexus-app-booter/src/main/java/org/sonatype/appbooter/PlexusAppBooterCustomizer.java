package org.sonatype.appbooter;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.sonatype.appcontext.AppContext;

/**
 * Extension points invoked during boot of Plexus container. The implementors of this interface should have
 * parameterless constructors, and should be able to be created by customizerClass.newInstance()!
 * 
 * @author cstamas
 */
public interface PlexusAppBooterCustomizer
{
    /**
     * Initially, the AppContext used to create PlexusContext will be passed it.
     * 
     * @param appBooter
     * @param context
     */
    void customizeContext( PlexusAppBooter appBooter, AppContext context );

    /**
     * Them the ContainerConfiguration is passed in for potential configuration changes/addons.
     * 
     * @param appBooter
     * @param containerConfiguration
     */
    void customizeContainerConfiguration( PlexusAppBooter appBooter, ContainerConfiguration containerConfiguration );

    /**
     * Finally, the created container is passed in.
     * 
     * @param appBooter
     * @param plexusContainer
     */
    void customizeContainer( PlexusAppBooter appBooter, PlexusContainer plexusContainer );
}
