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
import org.sonatype.guice.bean.inject.BeanBinder;
import org.sonatype.guice.bean.inject.PropertyBinder;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * {@link BeanBinder} that binds Plexus properties using annotations or other metadata.
 */
public final class PlexusComponentBinder
    implements BeanBinder
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <B> PropertyBinder bindBean( final TypeLiteral<B> type, final TypeEncounter<B> encounter )
    {
        final Component component = type.getRawType().getAnnotation( Component.class );
        if ( null != component )
        {
            // assume all other properties are marked with Plexus annotations
            return new PlexusAnnotatedPropertyBinder( encounter, component );
        }

        // TODO: PlexusMappedPropertyBinder

        return null;
    }
}
