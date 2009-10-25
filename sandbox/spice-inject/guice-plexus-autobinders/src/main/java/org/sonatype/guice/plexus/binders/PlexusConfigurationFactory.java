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
import org.sonatype.guice.plexus.config.Configurator;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.spi.TypeEncounter;

/**
 * Creates {@link Provider}s for property elements annotated with @{@link Configuration}.
 */
final class PlexusConfigurationFactory
    implements PropertyProviderFactory<Configuration>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final TypeEncounter<?> encounter;

    private final Key<Configurator> configuratorKey;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusConfigurationFactory( final TypeEncounter<?> encounter, final Component component )
    {
        this.encounter = encounter;

        // each Plexus component can have its own configurator bound to its role-hint
        configuratorKey = Key.get( Configurator.class, Roles.roleHint( component ) );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <T> Provider<T> lookup( final Configuration configuration, final BeanProperty<T> property )
    {
        final Provider<Configurator> configurator = getComponentConfigurator();

        final Configuration namedConfig;
        if ( configuration.name().length() == 0 )
        {
            // provided configuration doesn't have a name, so use the property name
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
                return configurator.get().configure( namedConfig, property.getType() );
            }
        };
    }

    /**
     * Returns a {@link Provider} that can provide a {@link Configurator} for the current component.
     * 
     * @return Provider that provides a configurator for the current component
     */
    private Provider<Configurator> getComponentConfigurator()
    {
        try
        {
            // first look for customized local configurator
            return encounter.getProvider( configuratorKey );
        }
        catch ( final RuntimeException e )
        {
            // fall-back to the globally defined configurator
            return encounter.getProvider( Configurator.class );
        }
    }
}
