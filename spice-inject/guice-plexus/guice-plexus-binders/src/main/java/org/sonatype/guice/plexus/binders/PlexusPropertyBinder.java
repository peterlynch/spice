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

import javax.inject.Provider;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.inject.PropertyBinder;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;

import com.google.inject.spi.TypeEncounter;

/**
 * {@link BeanPropertyBinder} that auto-binds properties according to Plexus metadata.
 */
final class PlexusPropertyBinder
    implements PropertyBinder
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final boolean OPTIONAL_SUPPORTED;

    static
    {
        boolean optionalSupported = true;
        try
        {
            // support both old and new forms of @Requirement
            Requirement.class.getDeclaredMethod( "optional" );
        }
        catch ( final Throwable e )
        {
            optionalSupported = false;
        }
        OPTIONAL_SUPPORTED = optionalSupported;
    }

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final PlexusBeanManager manager;

    private final PlexusBeanMetadata metadata;

    private final PlexusConfigurations configurations;

    private final PlexusRequirements requirements;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusPropertyBinder( final PlexusBeanManager manager, final TypeEncounter<?> encounter,
                          final PlexusBeanMetadata metadata )
    {
        this.manager = manager;
        this.metadata = metadata;

        configurations = new PlexusConfigurations( encounter );
        requirements = new PlexusRequirements( encounter );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <T> PropertyBinding bindProperty( final BeanProperty<T> property )
    {
        if ( metadata.isEmpty() )
        {
            return PropertyBinder.LAST_BINDING;
        }

        /*
         * @Configuration binding
         */
        final Configuration configuration = metadata.getConfiguration( property );
        if ( null != configuration )
        {
            final Provider<T> valueProvider = configurations.lookup( configuration, property );
            return new ProvidedPropertyBinding<T>( property, valueProvider );
        }

        /*
         * @Requirement binding
         */
        final Requirement requirement = metadata.getRequirement( property );
        if ( null != requirement )
        {
            if ( null != manager )
            {
                final PropertyBinding managedBinding = manager.manage( property );
                if ( null != managedBinding )
                {
                    return managedBinding; // the bean manager will handle this property
                }
            }
            final Provider<T> roleProvider = requirements.lookup( requirement, property );
            if ( OPTIONAL_SUPPORTED && requirement.optional() )
            {
                return new OptionalPropertyBinding<T>( property, roleProvider );
            }
            return new ProvidedPropertyBinding<T>( property, roleProvider );
        }

        return null; // nothing to bind
    }
}
