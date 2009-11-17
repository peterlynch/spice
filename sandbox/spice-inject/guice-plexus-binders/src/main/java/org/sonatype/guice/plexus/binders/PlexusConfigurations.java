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
import org.codehaus.plexus.component.annotations.Configuration;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.config.PlexusConfigurator;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * Creates {@link Provider}s for properties with @{@link Configuration} metadata.
 */
final class PlexusConfigurations
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    final Provider<RoleConfigurator> configurator;

    final Component component;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusConfigurations( final TypeEncounter<?> encounter, final Component component )
    {
        configurator = encounter.getProvider( RoleConfigurator.class );
        this.component = component;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /**
     * Creates a {@link Provider} that provides Plexus components that match the given property configuration.
     * 
     * @param requirement The Plexus configuration
     * @param property The bean property
     * @return Provider that provides configurations for the given property
     */
    public <T> Provider<T> lookup( final Configuration configuration, final BeanProperty<T> property )
    {
        final TypeLiteral<T> expectedType = property.getType();

        final Configuration namedConfig;
        if ( configuration.name().length() == 0 )
        {
            // provided configuration doesn't have a name, so use the property name instead
            namedConfig = new ConfigurationImpl( property.getName(), configuration.value() );
        }
        else
        {
            namedConfig = configuration;
        }

        return new Provider<T>()
        {
            public T get()
            {
                return configurator.get().forRole( component ).configure( namedConfig, expectedType );
            }
        };
    }

    // ----------------------------------------------------------------------
    // Implementation helpers
    // ----------------------------------------------------------------------

    /**
     * Redirects configuration requests to the relevant {@link PlexusConfigurator}.
     */
    @Singleton
    private static class RoleConfigurator
    {
        @Inject
        Injector injector;

        // not required if we only use per-component configuration
        @Inject( optional = true )
        PlexusConfigurator globalConfigurator;

        /**
         * Returns a {@link PlexusConfigurator} that can configure an instance of the given @{@link Component}.
         * 
         * @param component The Plexus component
         * @return PlexusConfigurator that can configure the given component role
         */
        PlexusConfigurator forRole( final Component component )
        {
            try
            {
                // support optional per-component configuration if required
                return injector.getInstance( Roles.configuratorKey( component ) );
            }
            catch ( final ConfigurationException e )
            {
                if ( globalConfigurator != null )
                {
                    return globalConfigurator;
                }
                throw new ProvisionException( "No implementation for PlexusConfigurator was bound" );
            }
        }
    }
}
