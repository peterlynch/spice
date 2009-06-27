package org.sonatype.plugin.metadata.plexus;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.sonatype.plugin.test.ManagedInterface;
import org.sonatype.plugin.test.ManagedViaInterface;

public class PlexusComponentGleanerTest
    extends PlexusTestCase
{
    public void testLoadComponent()
        throws Exception
    {
        String[] componentClassNames =
            { "org.sonatype.plugin.test.ManagedViaInterface", "org.sonatype.plugin.test.ComponentManaged" };

        PlexusComponentGleaner componentGleaner = lookup( PlexusComponentGleaner.class );

        PlexusComponentGleanerRequest request =
            new PlexusComponentGleanerRequest( "", this.getClass().getClassLoader() );

        for ( String componentClassName : componentClassNames )
        {
            request.setClassName( componentClassName );

            ComponentDescriptor<?> descriptor = componentGleaner.glean( request );

            if ( descriptor != null )
            {
                this.getContainer().addComponentDescriptor( descriptor );
            }
        }

        ManagedInterface component = (ManagedInterface) this.lookup( ManagedInterface.class );

        Assert.assertNotNull( component );

        ManagedViaInterface managedViaInterface = (ManagedViaInterface) component;

        Assert.assertNotNull( managedViaInterface.getMangedComponent() );

    }

}
