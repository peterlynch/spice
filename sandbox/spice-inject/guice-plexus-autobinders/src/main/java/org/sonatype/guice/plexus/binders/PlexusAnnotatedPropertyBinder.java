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
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.inject.PropertyBinder;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;

import com.google.inject.spi.TypeEncounter;

/**
 * {@link BeanPropertyBinder} that auto-binds properties according to Plexus annotations.
 */
final class PlexusAnnotatedPropertyBinder
    implements PropertyBinder
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final PlexusRequirementFactory requirementFactory;

    private final PlexusConfigurationFactory configurationFactory;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusAnnotatedPropertyBinder( final TypeEncounter<?> encounter, final Component component )
    {
        requirementFactory = new PlexusRequirementFactory( encounter /* , component */);
        configurationFactory = new PlexusConfigurationFactory( encounter, component );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <T> PropertyBinding bindProperty( final BeanProperty<T> property )
    {
        /*
         * @Requirement binding
         */
        final Requirement requirement = property.getAnnotation( Requirement.class );
        if ( null != requirement )
        {
            return new ProviderPropertyBinding<T>( property, requirementFactory.lookup( requirement, property ) );
        }

        /*
         * @Configuration binding
         */
        final Configuration configuration = property.getAnnotation( Configuration.class );
        if ( null != configuration )
        {
            return new ProviderPropertyBinding<T>( property, configurationFactory.lookup( configuration, property ) );
        }

        return null; // nothing to bind
    }
}
