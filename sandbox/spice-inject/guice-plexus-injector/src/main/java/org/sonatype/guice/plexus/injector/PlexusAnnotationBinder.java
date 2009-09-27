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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public final class PlexusAnnotationBinder
    implements TypeListener
{
    private PropertySource<Annotation>[] cachedPropertySources;

    @SuppressWarnings( "unchecked" )
    private PropertySource<Annotation>[] getPropertySources( final TypeEncounter<?> encounter )
    {
        if ( null == cachedPropertySources )
        {
            cachedPropertySources =
                new PropertySource[] { new RequirementSource( encounter ), new ConfigurationSource( encounter ) };
        }
        return cachedPropertySources;
    }

    public <T> void hear( final TypeLiteral<T> literal, final TypeEncounter<T> encounter )
    {
        final Collection<PropertyInjector> propertyInjectors = new ArrayList<PropertyInjector>();
        final PropertySource<Annotation>[] propertySources = getPropertySources( encounter );

        // iterate over all members in class hierarchy: constructors > methods > fields
        for ( final AnnotatedElement element : new AnnotatedElements( literal.getRawType() ) )
        {
            for ( final PropertySource<Annotation> source : propertySources )
            {
                final Annotation annotation = source.getAnnotation( element );
                if ( annotation != null )
                {
                    makeAccessible( (AccessibleObject) element );

                    if ( element instanceof Field )
                    {
                        final Field f = (Field) element;

                        final TypeLiteral<?> expectedType = TypeLiteral.get( f.getGenericType() );
                        final Provider<?> provider = source.getProvider( f.getName(), expectedType, annotation );
                        propertyInjectors.add( new ProvidedFieldInjector( f, provider ) );
                    }
                    else if ( element instanceof Method )
                    {
                        final Method m = (Method) element;

                        if ( m.getParameterTypes().length == 1 )
                        {
                            final TypeLiteral<?> expectedType = TypeLiteral.get( m.getGenericParameterTypes()[0] );
                            final Provider<?> provider = source.getProvider( m.getName(), expectedType, annotation );
                            propertyInjectors.add( new ProvidedParamInjector( m, provider ) );
                        }
                        else
                        {
                            encounter.addError( "Property method %s has wrong number of args", m.toGenericString() );
                        }
                    }
                }
            }
        }

        if ( !propertyInjectors.isEmpty() )
        {
            encounter.register( new ComponentInjector<Object>( propertyInjectors ) );
        }
    }

    private static final void makeAccessible( final AccessibleObject property )
    {
        if ( !property.isAccessible() )
        {
            AccessController.doPrivileged( new PrivilegedAction<Void>()
            {
                public Void run()
                {
                    property.setAccessible( true );
                    return null;
                }
            } );
        }
    }
}
