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
package org.sonatype.guice.plexus.injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.util.StringUtils;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.spi.TypeEncounter;

final class ConfigurationSource
    implements PropertySource<Configuration>
{
    private Provider<Injector> injectorProvider;

    private Map<String, String> cachedConfigMap;

    ConfigurationSource( final TypeEncounter<?> encounter )
    {
        injectorProvider = encounter.getProvider( Injector.class );
    }

    public Configuration getAnnotation( final AnnotatedElement element )
    {
        return element.getAnnotation( Configuration.class );
    }

    public Provider<?> getProvider( final String name, final TypeLiteral<?> type, final Configuration configuration )
    {
        final String key = configuration.name().length() == 0 ? name : configuration.name();
        final String defaultValue = configuration.value();

        return new Provider<String>()
        {
            public String get()
            {
                final Map<String, String> configMap = getConfigMap();
                final String value = configMap.get( key );

                return StringUtils.interpolate( null == value ? defaultValue : value, configMap );
            }
        };
    }

    Map<String, String> getConfigMap()
    {
        if ( null == cachedConfigMap )
        {
            final Injector injector = injectorProvider.get();
            injectorProvider = null;

            final List<Binding<String>> bindings = injector.findBindingsByType( TypeLiteral.get( String.class ) );

            final int numBindings = bindings.size();
            cachedConfigMap = new HashMap<String, String>( numBindings );
            for ( int i = 0; i < numBindings; i++ )
            {
                final Binding<String> b = bindings.get( i );
                final Annotation annotation = b.getKey().getAnnotation();
                if ( annotation instanceof Named )
                {
                    cachedConfigMap.put( ( (Named) annotation ).value(), b.getProvider().get() );
                }
            }
        }
        return cachedConfigMap;
    }
}
