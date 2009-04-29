package org.sonatype.appbooter;

import java.util.Map;

/**
 * A publisher to publish the Plexus context.
 * 
 * @author cstamas
 * @since 2.0
 */
public interface ContextPublisher
{
    void publishContext( PlexusAppBooter booter, Map<Object, Object> context )
        throws Exception;
}
