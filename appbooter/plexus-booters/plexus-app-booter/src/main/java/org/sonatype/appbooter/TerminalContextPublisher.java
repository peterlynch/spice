package org.sonatype.appbooter;

import java.util.Map;
import java.util.Map.Entry;

/**
 * A publisher that publishes Plexus Context to terminal by printing it's content out.
 * 
 * @author cstamas
 * @since 2.0
 */
public class TerminalContextPublisher
    implements ContextPublisher
{
    public void publishContext( PlexusAppBooter booter, Map<Object, Object> context )
        throws Exception
    {
        for ( Entry<Object, Object> entry : context.entrySet() )
        {
            // dump it to System.out
            System.out.println( "Property '" + String.valueOf( entry.getKey() ) + "'='"
                + String.valueOf( entry.getValue() ) + "' inserted into Plexus context." );
        }
    }
}
