package org.sonatype.guice.plexus.bindings;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class ComponentTest
    extends TestCase
{
    @Inject
    Injector injector;

    @Override
    protected void setUp()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                final Map<Class<?>, Component> components = new HashMap<Class<?>, Component>();

                addComponent( components, ComponentA.class );
                addComponent( components, ComponentD.class );

                install( new StaticPlexusBindingModule( components ) );

                components.clear();

                addComponent( components, ComponentB.class );
                addComponent( components, ComponentC.class );
                addComponent( components, ComponentE.class );

                install( new StaticPlexusBindingModule( components ) );
            }
        } ).injectMembers( this );
    }

    static void addComponent( final Map<Class<?>, Component> components, final Class<?> clazz )
    {
        components.put( clazz, clazz.getAnnotation( Component.class ) );
    }

    private static interface I
    {
    }

    @Component( role = I.class, hint = "A", instantiationStrategy = "per-lookup" )
    private static class ComponentA
        implements I
    {
    }

    @Component( role = I.class, hint = "B" )
    private static class ComponentB
        implements I
    {
    }

    @Component( role = I.class, hint = "C", instantiationStrategy = "per-lookup" )
    private static class ComponentC
        implements I
    {
    }

    @Component( role = I.class )
    private static class ComponentD
        implements I
    {
    }

    private static class ComponentE
        implements I
    {
    }

    public void testComponent()
    {
        final Key<I> keyA = Key.get( I.class, Names.named( "A" ) );
        final Key<I> keyB = Key.get( I.class, Names.named( "B" ) );
        final Key<I> keyC = Key.get( I.class, Names.named( "C" ) );

        assertTrue( injector.getInstance( keyA ) instanceof ComponentA );
        assertTrue( injector.getInstance( keyB ) instanceof ComponentB );
        assertTrue( injector.getInstance( keyC ) instanceof ComponentC );

        assertNotSame( injector.getInstance( keyA ), injector.getInstance( keyA ) );
        assertSame( injector.getInstance( keyB ), injector.getInstance( keyB ) );
        assertNotSame( injector.getInstance( keyC ), injector.getInstance( keyC ) );

        assertSame( injector.getInstance( I.class ), injector.getInstance( I.class ) );
    }
}
