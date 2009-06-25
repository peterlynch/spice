package org.sonatype.plugin.metadata.plexus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.sonatype.plugin.test.ManagedInterface;
import org.sonatype.plugin.test.ManagedViaInterface;
import org.sonatype.plugins.mock.MockExtensionPoint;
import org.sonatype.plugins.mock.MockManaged;

public class PlexusComponentGleanerTest
    extends PlexusTestCase
{
    
    public void testLoadComponent() throws Exception
    {
        
        String[] componentClassNames = {"org.sonatype.plugin.test.ManagedViaInterface", "org.sonatype.plugin.test.ComponentManaged"};
        
        List<Class<?>> componentMarkingAnnotations = new ArrayList<Class<?>>();
        componentMarkingAnnotations.add( MockExtensionPoint.class );
        componentMarkingAnnotations.add( MockManaged.class );
        
        PlexusComponentGleaner componentGleaner = new PlexusComponentGleaner( componentMarkingAnnotations );
        
        for ( String componentClassName : componentClassNames )
        {
            ComponentDescriptor<?> descriptor = componentGleaner.glean( componentClassName, this.getClass().getClassLoader() );
            
            this.getContainer().addComponentDescriptor( descriptor );
        }
        
        ManagedInterface component = (ManagedInterface) this.lookup( ManagedInterface.class );
        
        Assert.assertNotNull( component );
        
        ManagedViaInterface managedViaInterface = (ManagedViaInterface) component;
        
        Assert.assertNotNull( managedViaInterface.getMangedComponent() );
        
        
    }

}
