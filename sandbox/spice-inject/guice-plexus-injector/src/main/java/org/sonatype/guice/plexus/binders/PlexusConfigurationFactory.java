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
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.configuration.Configurator;
import org.sonatype.guice.plexus.utils.Hints;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeEncounter;

/**
 * Creates {@link Provider}s for property elements annotated with @{@link Configuration}.
 */
final class PlexusConfigurationFactory
    implements PropertyProviderFactory<Configuration>
{
    private final TypeEncounter<?> encounter;

    private final Key<Configurator> componentConfiguratorKey;

    PlexusConfigurationFactory( final TypeEncounter<?> encounter, final Component component )
    {
        this.encounter = encounter;

        final String role = component.role().getName();
        final String hint = component.hint();

        final String id = Hints.isDefaultHint( hint ) ? role : role + '/' + hint;

        componentConfiguratorKey = Key.get( Configurator.class, Names.named( id ) );
    }

    public Provider<?> lookup( final Configuration configuration, final InjectableProperty property )
    {
        final Configuration key;
        if ( configuration.name().length() == 0 )
        {
            key = new ConfigurationImpl( property.getName(), configuration.value() );
        }
        else
        {
            key = configuration;
        }

        final Provider<Configurator> configuratorProvider = encounter.getProvider( componentConfiguratorKey );

        return new Provider<Object>()
        {
            public Object get()
            {
                return configuratorProvider.get().configure( property.getType(), key );
            }
        };
    }
}
