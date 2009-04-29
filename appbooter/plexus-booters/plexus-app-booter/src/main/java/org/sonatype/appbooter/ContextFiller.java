package org.sonatype.appbooter;

import java.util.Map;

/**
 * A source to fill the Plexus Context.
 * 
 * @author cstamas
 * @since 2.0
 */
public interface ContextFiller
{
    void fillContext( PlexusAppBooter booter, Map<Object, Object> context )
        throws Exception;
}
