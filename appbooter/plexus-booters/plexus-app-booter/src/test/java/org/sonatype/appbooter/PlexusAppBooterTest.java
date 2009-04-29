package org.sonatype.appbooter;

import java.io.File;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import junit.framework.TestCase;

public class PlexusAppBooterTest
    extends TestCase
{
    public void testSimple()
        throws Exception
    {
        PlexusAppBooter pab = new PlexusAppBooter();

        pab.setBasedir( new File( "src/test/plexus" ) );

        pab.startContainer();

        assertNotNull( "Container is not created!", pab.getContainer() );

        try
        {
            pab.getContainer().lookup( String.class );
        }
        catch ( ComponentLookupException e )
        {
            // good
        }

        pab.stopContainer();
    }

}
