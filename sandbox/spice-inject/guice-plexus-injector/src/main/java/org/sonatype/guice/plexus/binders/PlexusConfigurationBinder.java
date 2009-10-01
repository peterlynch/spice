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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.util.StringUtils;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;

/**
 * Binds @{@link Configuration} properties to {@link Provider}s.
 */
public final class PlexusConfigurationBinder
    implements AnnotatedPropertyBinder<Configuration>
{
    private static final Type CONFIGURATION_TYPE = Types.mapOf( String.class, String.class );

    static final Key<Map<String, String>> CONFIGURATION_KEY = getConfigurationKey( Configuration.class.getName() );

    final Provider<Injector> injectorBinding;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusConfigurationBinder( final Provider<Injector> injectorBinding )
    {
        this.injectorBinding = injectorBinding;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Provider<?> bindProperty( final Configuration configuration, final TypeLiteral<?> type, final String fqn )
    {
        final int cursor = fqn.lastIndexOf( '.' );
        final String key = configuration.name().length() == 0 ? fqn.substring( cursor + 1 ) : configuration.name();
        final String defaultValue = configuration.value();

        return new Provider<String>()
        {
            public String get()
            {
                final Map<String, String> propertyMap = new HashMap<String, String>();
                propertyMap.putAll( injectorBinding.get().getInstance( CONFIGURATION_KEY ) );
                if ( cursor > 0 )
                {
                    final String namespace = fqn.substring( 0, cursor );
                    propertyMap.putAll( injectorBinding.get().getInstance( getConfigurationKey( namespace ) ) );
                }
                final String value = propertyMap.get( key );

                return StringUtils.interpolate( null == value ? defaultValue : value, propertyMap );
            }
        };
    }

    @SuppressWarnings( "unchecked" )
    static Key<Map<String, String>> getConfigurationKey( final String namespace )
    {
        return (Key) Key.get( CONFIGURATION_TYPE, Names.named( namespace ) );
    }
}
