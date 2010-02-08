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
package org.sonatype.guice.nexus.scanners;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.scanners.AnnotatedPlexusBeanSource;
import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;

public class AnnotatedNexusPluginTest
    extends TestCase
{
    interface BadInterface
    {
    }

    interface HostInterface0
    {
    }

    @Singleton
    interface UserInterface0
    {
    }

    @ExtensionPoint
    interface HostInterface1
    {
    }

    interface SubHostInterface1
        extends HostInterface1
    {
    }

    @Managed
    interface UserInterface1
    {
    }

    interface SubUserInterface1
        extends UserInterface1
    {
    }

    @Singleton
    @ExtensionPoint
    interface HostInterface2
    {
    }

    @Managed
    @Singleton
    interface UserInterface2
    {
    }

    @Component( role = HostInterface0.class )
    static class BeanA
        implements HostInterface0
    {
    }

    static class BeanB
        implements HostInterface1
    {
    }

    static class BeanC
        implements HostInterface2
    {
    }

    @Component( role = UserInterface0.class )
    static class BeanD
        implements UserInterface0
    {
    }

    static class BeanE
        implements UserInterface1
    {
    }

    static class BeanF
        implements UserInterface2
    {
    }

    @Named( "BeanA" )
    static class NamedBeanA
        implements HostInterface1
    {
    }

    @Named( "" )
    static class NamedBeanB
        implements HostInterface2
    {
    }

    @Named( "BeanC" )
    static class NamedBeanC
        implements UserInterface1
    {
    }

    @Named( "" )
    static class NamedBeanD
        implements UserInterface2
    {
    }

    @Component( role = SubHostInterface1.class )
    static class ComponentBeanA
        implements HostInterface1, SubHostInterface1
    {
    }

    @Component( role = SubUserInterface1.class )
    static class ComponentBeanB
        implements UserInterface1, SubUserInterface1
    {
    }

    static class BadBean
        implements BadInterface
    {
    }

    public void testComponentScanning()
    {
        final PlexusBeanSource source =
            new AnnotatedPlexusBeanSource( new URLClassSpace( (URLClassLoader) getClass().getClassLoader() ), null,
                                           new AnnotatedNexusComponentScanner() );

        final Map<Component, DeferredClass<?>> components = source.findPlexusComponentBeans();
        assertEquals( 11, components.size() );

        assertEquals( BeanA.class, components.get(
                                                   new ComponentImpl( HostInterface0.class, Hints.DEFAULT_HINT,
                                                                      "per-lookup", "" ) ).get() );
    }

    public void testBadClassFile()
    {
        System.setProperty( "java.protocol.handler.pkgs", getClass().getPackage().getName() );

        final ClassSpace parentSpace = new URLClassSpace( (URLClassLoader) getClass().getClassLoader() );
        final PlexusBeanSource source = new AnnotatedPlexusBeanSource( new ClassSpace()
        {
            public Class<?> loadClass( final String name )
                throws ClassNotFoundException
            {
                return parentSpace.loadClass( name );
            }

            public URL getResource( final String name )
            {
                if ( name.contains( "BadInterface" ) )
                {
                    try
                    {
                        return new URL( "barf:up/" );
                    }
                    catch ( MalformedURLException e )
                    {
                        throw new IllegalArgumentException( e.toString() );
                    }
                }
                return parentSpace.getResource( name );
            }

            public Enumeration<URL> getResources( final String name )
                throws IOException
            {
                return parentSpace.getResources( name );
            }

            public Enumeration<URL> findEntries( final String path, final String glob, final boolean recurse )
                throws IOException
            {
                return parentSpace.findEntries( path, glob, recurse );
            }

            public DeferredClass<?> deferLoadClass( final String name )
            {
                return parentSpace.deferLoadClass( name );
            }
        }, null, new AnnotatedNexusComponentScanner() );

        try
        {
            source.findPlexusComponentBeans();
            fail( "Expected RuntimeException" );
        }
        catch ( final RuntimeException e )
        {
            System.out.println( e );
        }
    }
}