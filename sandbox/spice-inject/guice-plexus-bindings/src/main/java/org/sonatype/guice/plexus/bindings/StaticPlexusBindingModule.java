package org.sonatype.guice.plexus.bindings;

import static com.google.inject.name.Names.named;

import java.util.Collection;

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
    private final Class<?>[] components;

    public StaticPlexusBindingModule( final Collection<Class<?>> components )
    {
        this.components = components.toArray( new Class[components.size()] );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    protected void configure()
    {
        for ( final Class<?> clazz : components )
        {
            final Component spec = clazz.getAnnotation( Component.class );
            if ( spec != null )
            {
                final Class role = spec.role();
                final String hint = spec.hint();

                final AnnotatedBindingBuilder abb = bind( role );
                final LinkedBindingBuilder lbb = hint.length() == 0 ? abb : abb.annotatedWith( named( hint ) );
                final ScopedBindingBuilder sbb = lbb.to( clazz );

                if ( !"per-lookup".equals( spec.instantiationStrategy() ) )
                {
                    sbb.in( Scopes.SINGLETON );
                }
            }
        }
    }
}
