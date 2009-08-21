package org.sonatype.plugin.metadata.plexus;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.plexus.PlexusTestCase;
import org.junit.Assert;
import org.sonatype.plugin.test.DefaultUserCustomComponent;
import org.sonatype.plugin.test.InjectListOfUserCustomComponents;
import org.sonatype.plugin.test.ManagedInterface;
import org.sonatype.plugin.test.ManagedViaInterface;
import org.sonatype.plugin.test.NamedUserCustomComponent;
import org.sonatype.plugin.test.UserCustomComponent;

public class PlexusComponentGleanerTest
    extends PlexusTestCase
{

    public void testLoadComponent()
        throws Exception
    {
        String[] componentClassNames =
            {
                "org.sonatype.plugin.test.ManagedViaInterface", "org.sonatype.plugin.test.ComponentManaged",
                "org.sonatype.plugin.test.DefaultUserCustomComponent",
                "org.sonatype.plugin.test.NamedUserCustomComponent"
            };

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

    public void test_injectList()
        throws Exception
    {
        String[] componentClassNames = {
            DefaultUserCustomComponent.class.getName(),
            NamedUserCustomComponent.class.getName(),
            InjectListOfUserCustomComponents.class.getName()
        };

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

        InjectListOfUserCustomComponents component =
            (InjectListOfUserCustomComponents) this.lookup( ManagedInterface.class );

        assertNotNull( component );

        verify( component.injectedViaInject, component.injectedViaRequirement );
        verify( component.getPackageInjectedViaInject(), component.injectedViaRequirement );
        verify( component.getPackageInjectedViaInject(), component.injectedViaRequirement );

        // check that an Inject on an not specified generic type works but does not inject anything
        assertNotNull( component.unknownRole );
        assertEquals( "Inected list expected empty", 0, component.unknownRole.size() );

    }

    private void verify( final List<UserCustomComponent> injectedViaInject,
                         final List<UserCustomComponent> injectedViaRequirement )
    {
        assertNotNull( injectedViaInject );

        List<UserCustomComponent> copy = new ArrayList<UserCustomComponent>( injectedViaRequirement );
        copy.removeAll( injectedViaInject );

        assertTrue( "Inject via @Inject different then via @Requirement", copy.size() > 0 );
    }

}
