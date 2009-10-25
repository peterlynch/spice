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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.injector.BeanBinder;
import org.sonatype.guice.bean.injector.BeanPropertyBinder;

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

    public <T> BeanPropertyBinder bindComponent( final TypeEncounter<T> encounter, final TypeLiteral<T> type )
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

    // ----------------------------------------------------------------------
    // Shared package-private methods
    // ----------------------------------------------------------------------

    /**
     * Creates a new {@link InjectableProperty} backed by the given {@link AnnotatedElement}.
     * 
     * @param element The annotated element
     * @return Injectable property for the given element
     */
    static InjectableProperty<Object> newInjectableProperty( final AnnotatedElement element )
    {
        if ( element instanceof Field )
        {
            return new InjectableFieldProperty<Object>( (Field) element );
        }
        if ( element instanceof Method )
        {
            return new InjectableParamProperty<Object>( (Method) element );
        }
        throw new IllegalArgumentException( "Unexpected Plexus property type " + element );
    }
}
