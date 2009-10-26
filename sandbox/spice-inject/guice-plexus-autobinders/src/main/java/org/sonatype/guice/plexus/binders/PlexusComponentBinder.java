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

import java.util.Collections;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.inject.BeanBinder;
import org.sonatype.guice.bean.inject.PropertyBinder;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusAnnotations;
import org.sonatype.guice.plexus.config.PlexusComponents;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * {@link BeanBinder} that binds Plexus properties using annotations or other metadata.
 */
public final class PlexusComponentBinder
    implements BeanBinder
{
    private final PlexusComponents components;

    public PlexusComponentBinder()
    {
        components = new AnnotatedPlexusComponents();
    }

    public PlexusComponentBinder( final PlexusComponents components )
    {
        this.components = components;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <B> PropertyBinder bindBean( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
    {
        final PlexusAnnotations annotations = components.getAnnotations( type.getRawType() );
        if ( null != annotations )
        {
            // assume all other properties are marked with Plexus annotations
            return new PlexusAnnotatedPropertyBinder( encounter, annotations );
        }
        return null;
    }

    static class AnnotatedPlexusComponents
        implements PlexusComponents
    {
        public Iterable<Class<?>> getComponents()
        {
            return Collections.emptyList();
        }

        public PlexusAnnotations getAnnotations( final Class<?> implementation )
        {
            if ( implementation.isAnnotationPresent( Component.class ) )
            {
                return new PlexusAnnotations()
                {
                    public Component getComponent()
                    {
                        return implementation.getAnnotation( Component.class );
                    }

                    public Configuration getConfiguration( BeanProperty<?> property )
                    {
                        return property.getAnnotation( Configuration.class );
                    }

                    public Requirement getRequirement( BeanProperty<?> property )
                    {
                        return property.getAnnotation( Requirement.class );
                    }
                };
            }

            return null;
        }
    }
}
