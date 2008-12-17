package org.sonatype.plexus.jetty.custom;

import org.codehaus.plexus.PlexusContainer;
import org.sonatype.plexus.jetty.DefaultServletContainer;

/**
 * Manages a ThreadLocal, and makes the contained PlexusContainer available to Jetty LifeCycle.Listener
 * implementations. The ThreadLocal should be managed by whatever code initializes the Jetty Server instance, 
 * such as {@link DefaultServletContainer}.
 * 
 * @author jdcasey
 *
 */
public class PlexusContainerHolder
{
    
    private static ThreadLocal<PlexusContainer> containerLocal = new ThreadLocal<PlexusContainer>();
    
    public static void set( PlexusContainer container )
    {
        containerLocal.set( container );
    }
    
    public static PlexusContainer get()
    {
        return containerLocal.get();
    }

    public static void clear()
    {
        containerLocal.set( null );
    }

}
