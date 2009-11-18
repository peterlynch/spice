/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.guice.plexus.binders;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.inject.BeanBinder;
import org.sonatype.guice.bean.inject.BeanListener;
import org.sonatype.guice.bean.inject.PropertyBinder;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Guice {@link Module} that registers a custom {@link TypeListener} to auto-bind Plexus beans.
 */
public final class PlexusBindingModule
    extends AbstractModule
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String MISSING_METADATA_ERROR = "Component %s has no Plexus metadata.";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final PlexusBeanSource[] sources;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusBindingModule()
    {
        // default "just-in-time" metadata
        this( new AnnotatedBeanSource() );
    }

    /**
     * @param sources The available Plexus bean sources
     */
    public PlexusBindingModule( final PlexusBeanSource... sources )
    {
        this.sources = sources.clone();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    protected void configure()
    {
        for ( final PlexusBeanSource source : sources )
        {
            for ( final Class<?> clazz : source.findBeanImplementations() )
            {
                // explicitly bind bean implementations according to their metadata
                final PlexusBeanMetadata metadata = source.getBeanMetadata( clazz );
                if ( metadata != null )
                {
                    bindPlexusBean( clazz, metadata );
                }
                else
                {
                    addError( MISSING_METADATA_ERROR, clazz );
                }
            }
        }

        // mechanism to inject Plexus requirements and configurations into beans
        bindListener( Matchers.any(), new BeanListener( new PlexusBeanBinder() ) );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Binds the given bean implementation according to the given Plexus metadata.
     * 
     * @param clazz The bean implementation
     * @param metadata The Plexus metadata
     */
    @SuppressWarnings( "unchecked" )
    private void bindPlexusBean( final Class clazz, final PlexusBeanMetadata metadata )
    {
        // we want to bind the role to the implementation
        final Component component = metadata.getComponent();
        final Key roleKey = Roles.componentKey( component );
        final Key beanKey = Key.get( clazz );

        final ScopedBindingBuilder sbb = roleKey.equals( beanKey ) ? bind( beanKey ) : bind( roleKey ).to( beanKey );

        // singleton is the default strategy for Plexus beans
        final String strategy = component.instantiationStrategy();
        if ( "load-on-start".equals( strategy ) )
        {
            sbb.asEagerSingleton();
        }
        else if ( !"per-lookup".equals( strategy ) )
        {
            sbb.in( Scopes.SINGLETON );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation helpers
    // ----------------------------------------------------------------------

    /**
     * {@link BeanBinder} that auto-binds beans according to Plexus metadata.
     */
    final class PlexusBeanBinder
        implements BeanBinder
    {
        public <B> PropertyBinder bindBean( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
        {
            final Class<?> clazz = type.getRawType();
            for ( final PlexusBeanSource source : sources )
            {
                // use first source that has metadata for the given implementation
                final PlexusBeanMetadata metadata = source.getBeanMetadata( clazz );
                if ( metadata != null )
                {
                    return new PlexusPropertyBinder( encounter, metadata );
                }
            }
            return null; // no need to auto-bind
        }
    }
}
