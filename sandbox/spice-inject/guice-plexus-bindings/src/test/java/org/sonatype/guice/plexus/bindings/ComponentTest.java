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
package org.sonatype.guice.plexus.bindings;

import java.util.Arrays;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusAnnotations;
import org.sonatype.guice.plexus.config.PlexusComponents;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class ComponentTest
    extends TestCase
{
    @Inject
    Injector injector;

    static class BasicComponentMap
        implements PlexusComponents
    {
        private final Iterable<Class<?>> components;

        public BasicComponentMap( Class<?>... components )
        {
            this.components = Arrays.asList( components );
        }

        public Iterable<Class<?>> getComponents()
        {
            return components;
        }

        public PlexusAnnotations getAnnotations( final Class<?> implementation )
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
    }

    @Override
    protected void setUp()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                install( new PlexusStaticBindings( new BasicComponentMap( ComponentA.class, ComponentD.class,
                                                                          ComponentB.class, ComponentC.class ) ) );
            }
        } ).injectMembers( this );
    }

    private static interface I
    {
    }

    @Component( role = I.class, hint = "A", instantiationStrategy = "per-lookup" )
    private static class ComponentA
        implements I
    {
    }

    @Component( role = I.class, hint = "B" )
    private static class ComponentB
        implements I
    {
    }

    @Component( role = I.class, hint = "C", instantiationStrategy = "per-lookup" )
    private static class ComponentC
        implements I
    {
    }

    @Component( role = I.class )
    private static class ComponentD
        implements I
    {
    }

    public void testComponent()
    {
        final Key<I> keyA = Key.get( I.class, Names.named( "A" ) );
        final Key<I> keyB = Key.get( I.class, Names.named( "B" ) );
        final Key<I> keyC = Key.get( I.class, Names.named( "C" ) );

        assertTrue( injector.getInstance( keyA ) instanceof ComponentA );
        assertTrue( injector.getInstance( keyB ) instanceof ComponentB );
        assertTrue( injector.getInstance( keyC ) instanceof ComponentC );

        assertNotSame( injector.getInstance( keyA ), injector.getInstance( keyA ) );
        assertSame( injector.getInstance( keyB ), injector.getInstance( keyB ) );
        assertNotSame( injector.getInstance( keyC ), injector.getInstance( keyC ) );

        assertSame( injector.getInstance( I.class ), injector.getInstance( I.class ) );
    }
}
