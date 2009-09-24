package org.sonatype.guice.plexus.bindings;

import static com.google.inject.name.Names.named;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

public class StaticPlexusBindingModule
    extends AbstractModule
{
    private final Iterable<Class<?>> components;

    private final Map<Class<?>, MapBinder<String, ?>> mapBinders = new HashMap<Class<?>, MapBinder<String, ?>>();

    public StaticPlexusBindingModule( final Iterable<Class<?>> components )
    {
        this.components = components;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    protected void configure()
    {
        for ( final Class<?> clazz : components )
        {
            final Component spec = clazz.getAnnotation( Component.class );

            final Class role = spec.role();
            final String hint = spec.hint();

            final ScopedBindingBuilder singleBinding = singleBinding( role, hint ).to( clazz );
            final ScopedBindingBuilder multiBinding = multiBinding( role, hint ).to( clazz );

            if ( "per-lookup".equals( spec.instantiationStrategy() ) == false )
            {
                singleBinding.in( Scopes.SINGLETON );
                multiBinding.in( Scopes.SINGLETON );
            }
        }
    }

    private final <T> LinkedBindingBuilder<T> singleBinding( final Class<T> role, final String hint )
    {
        return ( hint == null || hint.length() == 0 ) ? bind( role ) : bind( role ).annotatedWith( named( hint ) );
    }

    private final <T> LinkedBindingBuilder<T> multiBinding( final Class<T> role, final String hint )
    {
        @SuppressWarnings( "unchecked" )
        MapBinder<String, T> mapBinder = (MapBinder<String, T>) mapBinders.get( role );
        if ( null == mapBinder )
        {
            mapBinder = MapBinder.newMapBinder( binder(), String.class, role );
            mapBinders.put( role, mapBinder );
        }
        return mapBinder.addBinding( hint );
    }
}
