package org.sonatype.appbooter;

import java.util.Map;

/**
 * A ContextFiller that uses System.getenv() as source, filters it for environment variable names starting with
 * "PLEXUS_".
 * 
 * @author cstamas
 * @since 2.0
 */
public class SystemEnvironmentContextFiller
    implements ContextFiller
{
    public void fillContext( PlexusAppBooter booter, Map<Object, Object> context )
    {
        /*
         * Iterate through environment variables, insert all items into a map (making sure to do translation needed,
         * remove "PLEXUS_" , change all _ to - and convert to lower case)
         */
        Map<String, String> envMap = System.getenv();

        String envVarPrefix = getKeyPrefix( booter );

        for ( String key : envMap.keySet() )
        {
            if ( key.toUpperCase().startsWith( envVarPrefix ) && key.length() > envVarPrefix.length() )
            {
                String plexusKey = key.toLowerCase().substring( envVarPrefix.length() ).replace( '_', '-' );

                context.put( plexusKey, envMap.get( key ) );
            }
        }
    }

    protected String getKeyPrefix( PlexusAppBooter booter )
    {
        return booter.getName().toUpperCase() + "_";
    }
}
