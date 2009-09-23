package org.sonatype.guice.plexus.bindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
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
                final Collection<Class<?>> components = new ArrayList<Class<?>>();

                components.add( ComponentA.class );

                install( new StaticPlexusBindingModule( components ) );

                components.clear();
                components.add( ComponentB.class );
                components.add( ComponentC.class );

                install( new StaticPlexusBindingModule( components ) );
            }
        } ).injectMembers( this );
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

    public void testComponent()
    {
        final Key<Map<String, I>> mapKey = Key.get( new TypeLiteral<Map<String, I>>()
        {
        } );

        final Key<I> keyA = Key.get( I.class, Names.named( "A" ) );
        final Key<I> keyB = Key.get( I.class, Names.named( "B" ) );
        final Key<I> keyC = Key.get( I.class, Names.named( "C" ) );

        assertTrue( injector.getInstance( keyA ) instanceof ComponentA );
        assertTrue( injector.getInstance( keyB ) instanceof ComponentB );
        assertTrue( injector.getInstance( keyC ) instanceof ComponentC );

        assertNotSame( injector.getInstance( keyA ), injector.getInstance( keyA ) );
        assertSame( injector.getInstance( keyB ), injector.getInstance( keyB ) );
        assertNotSame( injector.getInstance( keyC ), injector.getInstance( keyC ) );

        try
        {
            injector.getInstance( I.class );
            fail();
        }
        catch ( final ConfigurationException e )
        {
        }

        final Map<String, I> map = injector.getInstance( mapKey );

        assertEquals( map.size(), 3 );

        assertTrue( map.get( "A" ) instanceof ComponentA );
        assertTrue( map.get( "B" ) instanceof ComponentB );
        assertTrue( map.get( "C" ) instanceof ComponentC );
    }
}
