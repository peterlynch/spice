package org.sonatype.appbooter;

import java.util.Map;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;

public class PlexusContainerHostTest
    extends PlexusTestCase
{
    public Map<Object, Object> createContainerContext()
        throws Exception
    {
        System.setProperty( "basedir", getBasedir() + "/src/test/plexus" );

        ClassWorld cw = new ClassWorld();

        PlexusContainerHost pch = new PlexusContainerHost( cw );

        return pch.createContainerContext();
    }

    public void testCreateContainerContextSimple()
        throws Exception
    {
	    // skipping test because of potential to fail based upon host's environment settings
		return;
		
		/*
        Map<Object, Object> ctx = createContainerContext();

        assertEquals( true, ctx.get( "nexus-work" ).toString().endsWith( "/sonatype-work/nexus" ) );
		*/
    }

    public void testCreateContainerContextSystemPropsOverride()
        throws Exception
    {
	    // skipping test because of potential to fail based upon host's environment settings
		return;
		
		/*
        System.setProperty( "plexus.nexus-work", "ukulele" );

        Map<Object, Object> ctx = createContainerContext();

        assertEquals( false, ctx.get( "nexus-work" ).toString().endsWith( "/sonatype-work/nexus" ) );

        assertEquals( "ukulele", ctx.get( "nexus-work" ) );

        System.getProperties().remove( "plexus.nexus-work" );
		*/
    }

}
