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

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.plexus.injector.PropertyBinder;
import org.sonatype.guice.plexus.injector.PropertyInjector;

import com.google.inject.spi.TypeEncounter;

public final class PlexusPropertyBinder
    implements PropertyBinder
{
    public PropertyInjector bindProperty( final TypeEncounter<?> encounter, final AnnotatedElement element )
    {
        try
        {
            final Requirement req = element.getAnnotation( Requirement.class );
            if ( null != req )
            {
                final PlexusProperty property = getPlexusProperty( element );
                return property.bind( PlexusRequirement.getProvider( encounter, req, property ) );
            }
            final Configuration conf = element.getAnnotation( Configuration.class );
            if ( null != conf )
            {
                final PlexusProperty property = getPlexusProperty( element );
                return property.bind( PlexusConfiguration.getProvider( encounter, conf, property ) );
            }
        }
        catch ( final RuntimeException e )
        {
            encounter.addError( e.toString() );
        }
        return null;
    }

    private static PlexusProperty getPlexusProperty( final AnnotatedElement element )
    {
        if ( element instanceof Field )
        {
            return new PlexusFieldProperty( (Field) element );
        }
        if ( element instanceof Method )
        {
            return new PlexusParamProperty( (Method) element );
        }
        throw new IllegalArgumentException();
    }
}
