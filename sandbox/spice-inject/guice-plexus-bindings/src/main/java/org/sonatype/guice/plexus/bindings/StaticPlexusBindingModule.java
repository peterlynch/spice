package org.sonatype.guice.plexus.bindings;

import static com.google.inject.name.Names.named;
import static org.sonatype.guice.plexus.utils.Hints.isDefaultHint;

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
public final class StaticPlexusBindingModule
    extends AbstractModule
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<Class<?>, Component> componentMap;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public StaticPlexusBindingModule( final Map<Class<?>, Component> componentMap )
    {
        this.componentMap = componentMap;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    @SuppressWarnings( "unchecked" )
    protected void configure()
    {
        for ( final Entry<Class<?>, Component> e : componentMap.entrySet() )
        {
            final Class<?> clazz = e.getKey();
            final Component spec = e.getValue();
            final String hint = spec.hint();

            // bind role + optional hint -> implementation
            final AnnotatedBindingBuilder abb = bind( spec.role() );
            final LinkedBindingBuilder lbb = isDefaultHint( hint ) ? abb : abb.annotatedWith( named( hint ) );
            final ScopedBindingBuilder sbb = lbb.to( clazz );

            // assume anything other than "per-lookup" is a singleton
            if ( !"per-lookup".equals( spec.instantiationStrategy() ) )
            {
                sbb.in( Scopes.SINGLETON );
            }
        }
    }
}
