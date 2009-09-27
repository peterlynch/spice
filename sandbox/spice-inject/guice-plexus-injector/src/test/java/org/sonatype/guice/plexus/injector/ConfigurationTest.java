package org.sonatype.guice.plexus.injector;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

/**
 * Test various Plexus {@link Requirement} use-cases.
 */
public class ConfigurationTest
    extends TestCase
{
    @Inject
    PlainComponent plainComponent;

    @Inject
    InterpolatedComponent interpolatedComponent;

    @Override
    protected void setUp()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindConstant().annotatedWith( Names.named( "plainConfig" ) ).to( "something" );
                bindConstant().annotatedWith( Names.named( "testConfig" ) ).to( "This ${blank} a test" );
                bindConstant().annotatedWith( Names.named( "blank" ) ).to( "is" );

                bindListener( Matchers.any(), new PlexusAnnotationBinder() );
            }
        } ).injectMembers( this );
    }

    static class PlainComponent
    {
        @Configuration( "default" )
        String defaultConfig;

        @Configuration( "default" )
        String plainConfig;

        @Configuration( name = "plainConfig", value = "default" )
        String renamedConfig;
    }

    static class InterpolatedComponent
    {
        @Configuration( "This ${blank} a test" )
        String defaultConfig;

        @Configuration( "This ${blank} a test" )
        String plainConfig;

        @Configuration( "This ${blank} a test" )
        String testConfig;
    }

    public void testBasicConfiguration()
    {
        assertEquals( "default", plainComponent.defaultConfig );
        assertEquals( "something", plainComponent.plainConfig );
        assertEquals( "something", plainComponent.renamedConfig );
    }

    public void testInterpolatedConfiguration()
    {
        assertEquals( "This is a test", interpolatedComponent.defaultConfig );
        assertEquals( "something", interpolatedComponent.plainConfig );
        assertEquals( "This is a test", interpolatedComponent.testConfig );
    }
}
