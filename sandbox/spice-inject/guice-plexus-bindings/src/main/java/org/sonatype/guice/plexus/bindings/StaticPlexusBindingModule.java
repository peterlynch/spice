package org.sonatype.guice.plexus.bindings;

import static com.google.inject.name.Names.named;
import static org.sonatype.guice.plexus.utils.PlexusConstants.isDefaultHint;

import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Named;

/**
 * Guice {@link Module} that converts Plexus components into {@link Named} bindings.
 */
public class StaticPlexusBindingModule
    extends AbstractModule
{
    private final Map<Class<?>, Component> components;

    public StaticPlexusBindingModule( final Map<Class<?>, Component> components )
    {
        this.components = components;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    protected void configure()
    {
        for ( final Entry<Class<?>, Component> e : components.entrySet() )
        {
            final Class<?> clazz = e.getKey();
            final Component spec = e.getValue();

            if ( null == spec )
            {
                continue;
            }

            final Class role = spec.role();
            final String hint = spec.hint();

            final AnnotatedBindingBuilder abb = bind( role );
            final LinkedBindingBuilder lbb = isDefaultHint( hint ) ? abb : abb.annotatedWith( named( hint ) );
            final ScopedBindingBuilder sbb = lbb.to( clazz );

            if ( !"per-lookup".equals( spec.instantiationStrategy() ) )
            {
                sbb.in( Scopes.SINGLETON );
            }
        }
    }
}
