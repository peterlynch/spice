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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.plexus.component.annotations.Requirement;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * {@link TypeListener} that listens for types with {@link Requirement}s and wires them up to Guice.
 */
public final class PlexusAnnotationListener
    implements TypeListener
{
    public <T> void hear( final TypeLiteral<T> literal, final TypeEncounter<T> encounter )
    {
        final Collection<PropertyInjector> injectors = new ArrayList<PropertyInjector>();

        // iterate over all members in class hierarchy: constructors > methods > fields
        for ( final AnnotatedElement e : new AnnotatedElements( literal.getRawType() ) )
        {
            if ( e.isAnnotationPresent( Requirement.class ) )
            {
                if ( e instanceof Field )
                {
                    injectors.add( new RequirementFieldSetter( encounter, (Field) e ) );
                }
                else if ( e instanceof Method )
                {
                    final Method m = (Method) e;
                    if ( m.getParameterTypes().length == 1 )
                    {
                        injectors.add( new RequirementMethodSetter( encounter, m ) );
                    }
                    else
                    {
                        encounter.addError( "Requirement setter %s has wrong number of args", m.toGenericString() );
                    }
                }
            }
        }

        if ( injectors.size() > 0 )
        {
            // pass wiring information to Guice so it can apply it later on
            encounter.register( new ComponentInjector<T>( injectors ) );
        }
    }
}
