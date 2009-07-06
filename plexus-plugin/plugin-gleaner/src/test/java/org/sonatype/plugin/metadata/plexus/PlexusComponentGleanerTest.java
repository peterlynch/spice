package org.sonatype.plugin.metadata.plexus;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.plugin.test.ManagedInterface;
import org.sonatype.plugin.test.ManagedViaInterface;

public class PlexusComponentGleanerTest
    extends PlexusTestCase
{
    public void testLoadComponent()
        throws Exception
    {
        String[] componentClassNames =
            { "org.sonatype.plugin.test.ManagedViaInterface", "org.sonatype.plugin.test.ComponentManaged",
                "org.sonatype.plugin.test.DefaultUserCustomComponent",
                "org.sonatype.plugin.test.NamedUserCustomComponent" };

        PlexusComponentGleaner componentGleaner = lookup( PlexusComponentGleaner.class );

        PlexusComponentGleanerRequest request =
            new PlexusComponentGleanerRequest( "", this.getClass().getClassLoader() );

        for ( String componentClassName : componentClassNames )
        {
            request.setClassName( componentClassName );

            PlexusComponentGleanerResponse response = componentGleaner.glean( request );

            if ( response != null )
            {
                this.getContainer().addComponentDescriptor( response.getComponentDescriptor() );
            }
        }

        ManagedInterface component = (ManagedInterface) this.lookup( ManagedInterface.class );

        Assert.assertNotNull( component );

        ManagedViaInterface managedViaInterface = (ManagedViaInterface) component;

        Assert.assertNotNull( managedViaInterface.getMangedComponent() );

        assertEquals( "default", managedViaInterface.getUserCustomComponent().getMessage() );

        assertEquals( "another", managedViaInterface.getNamedUserCustomComponent().getMessage() );

    }

}
