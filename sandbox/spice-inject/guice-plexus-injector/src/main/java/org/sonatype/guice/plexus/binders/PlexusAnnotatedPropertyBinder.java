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

import static org.sonatype.guice.plexus.binders.PlexusComponentBinder.newInjectableProperty;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.injector.BeanPropertyBinder;
import org.sonatype.guice.bean.injector.BeanPropertyBinding;

import com.google.inject.spi.TypeEncounter;

/**
 * {@link BeanPropertyBinder} that auto-binds properties according to Plexus annotations.
 */
final class PlexusAnnotatedPropertyBinder
    implements BeanPropertyBinder
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

    public BeanPropertyBinding bindProperty( final Member member )
    {
        final AnnotatedElement element = (AnnotatedElement) member;

        /*
         * @Requirement binding
         */
        final Requirement requirement = element.getAnnotation( Requirement.class );
        if ( null != requirement )
        {
            final InjectableProperty<Object> property = newInjectableProperty( element );
            return property.bind( requirementFactory.lookup( requirement, property ) );
        }

        /*
         * @Configuration binding
         */
        final Configuration configuration = element.getAnnotation( Configuration.class );
        if ( null != configuration )
        {
            final InjectableProperty<Object> property = newInjectableProperty( element );
            return property.bind( configurationFactory.lookup( configuration, property ) );
        }

        return null; // nothing to bind
    }
}
