package org.sonatype.appbooter;

import java.util.Map;
import java.util.Map.Entry;

/**
 * A publisher that publishes Plexus Context to System properties prefixed with "plexus.", to make it available for
 * other system components like loggers, caches, etc.
 * 
 * @author cstamas
 * @since 2.0
 */
public class SystemPropertiesContextPublisher
    implements ContextPublisher
{
    public void publishContext( PlexusAppBooter booter, Map<Object, Object> context )
        throws Exception
    {
        for ( Entry<Object, Object> entry : context.entrySet() )
        {
            String key = (String) entry.getKey();

            String value = (String) entry.getValue();

            // adjust the key name and put it back to System properties
            String sysPropKey = "plexus." + key;

            if ( System.getProperty( sysPropKey ) == null )
            {
                System.setProperty( sysPropKey, (String) value );
            }
        }
    }
}
