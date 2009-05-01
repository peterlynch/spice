package org.sonatype.appbooter;

import java.util.Map;
import java.util.Properties;

/**
 * A ContextFiller that uses System.getProperties() as source, filters it for property keys starting with "plexus.".
 * 
 * @author cstamas
 * @since 2.0
 */
public class SystemPropertiesContextFiller
    implements ContextFiller
{
    public void fillContext( PlexusAppBooter booter, Map<Object, Object> context )
    {
        /*
         * Iterate through system properties, insert all items into a map (making sure to do the translation needed,
         * remove "plexus." )
         */
        Properties sysProps = System.getProperties();

        String systemPropPrefix = getKeyPrefix( booter );

        for ( Object obj : sysProps.keySet() )
        {
            String key = obj.toString();

            if ( key.startsWith( systemPropPrefix ) && key.length() > systemPropPrefix.length() )
            {
                String plexusKey = key.substring( systemPropPrefix.length() );

                context.put( plexusKey, sysProps.get( obj ) );
            }
        }
    }

    protected String getKeyPrefix( PlexusAppBooter booter )
    {
        return booter.getName() + ".";
    }
}
