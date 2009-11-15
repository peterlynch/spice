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
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

/**
 * {@link PlexusBeanSource} that provides Plexus metadata based on runtime annotations.
 */
public class AnnotatedBeanSource
    implements PlexusBeanSource
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Iterable<Class<?>> findBeanImplementations()
    {
        return Collections.emptyList(); // this source doesn't do any scanning
    }

    public final PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
    {
        final Component component = implementation.getAnnotation( Component.class );
        if ( null == component )
        {
            return null; // don't provide metadata for beans without @Component
        }

        return new PlexusBeanMetadata()
        {
            public Component getComponent()
            {
                return component;
            }

            public Configuration getConfiguration( final BeanProperty<?> property )
            {
                return property.getAnnotation( Configuration.class );
            }

            public Requirement getRequirement( final BeanProperty<?> property )
            {
                return property.getAnnotation( Requirement.class );
            }
        };
    }
}
