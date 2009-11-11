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

import java.util.Arrays;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.annotations.ConfigurationImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.PlexusConfigurator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Test additional Plexus metadata use-cases.
 */
public class PlexusBeanMetadataTest
    extends TestCase
{
    @Inject
    @Named( "2" )
    Bean bean;

    @Inject
    Injector injector;

    @Override
    protected void setUp()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bindConstant().annotatedWith( Names.named( "KEY1" ) ).to( "REQUIREMENT" );
                bindConstant().annotatedWith( Names.named( "KEY2" ) ).to( "CONFIGURATION" );
                bind( PlexusConfigurator.class ).to( DummyConfigurator.class );
                install( new PlexusBindingModule( new ExtraBeanSource() ) );
            }
        } ).injectMembers( this );
    }

    interface Bean
    {
        Object getExtraMetadata();

        void setExtraMetadata( Object metadata );
    }

    static class DefaultBean1
        implements Bean
    {
        Object testMetadata;

        public Object getExtraMetadata()
        {
            return testMetadata;
        }

        public void setExtraMetadata( final Object metadata )
        {
            testMetadata = metadata;
        }
    }

    static class DefaultBean2
    {
        String extraMetadata;
    }

    static class DummyConfigurator
        implements PlexusConfigurator
    {
        @Inject
        Injector injector;

        public <T> T configure( final Configuration configuration, final TypeLiteral<T> expectedType )
        {
            return injector.getInstance( Key.get( expectedType, Names.named( configuration.name() ) ) );
        }
    }

    static class ExtraBeanSource
        implements PlexusBeanSource
    {
        public Iterable<Class<?>> findBeanImplementations()
        {
            return Arrays.<Class<?>> asList( DefaultBean1.class, DefaultBean2.class );
        }

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            if ( DefaultBean1.class.equals( implementation ) )
            {
                return new PlexusBeanMetadata()
                {
                    public Component getComponent()
                    {
                        return new ComponentImpl( Bean.class, "2", "singleton" );
                    }

                    public Requirement getRequirement( final BeanProperty<?> property )
                    {
                        if ( "extraMetadata".equals( property.getName() ) )
                        {
                            return new RequirementImpl( String.class, false, "KEY1" );
                        }
                        return null;
                    }

                    public Configuration getConfiguration( final BeanProperty<?> property )
                    {
                        return null;
                    }
                };
            }
            if ( DefaultBean2.class.equals( implementation ) )
            {
                return new PlexusBeanMetadata()
                {
                    public Component getComponent()
                    {
                        return new ComponentImpl( DefaultBean2.class, "", "per-lookup" );
                    }

                    public Requirement getRequirement( final BeanProperty<?> property )
                    {
                        return null;
                    }

                    public Configuration getConfiguration( final BeanProperty<?> property )
                    {
                        if ( "extraMetadata".equals( property.getName() ) )
                        {
                            return new ConfigurationImpl( "KEY2", "TEST" );
                        }
                        return null;
                    }
                };
            }
            return null;
        }
    }

    public void testExtraMetadata()
    {
        assertSame( bean, injector.getInstance( Key.get( Bean.class, Names.named( "2" ) ) ) );

        assertEquals( "REQUIREMENT", bean.getExtraMetadata() );

        assertEquals( "CONFIGURATION", injector.getInstance( DefaultBean2.class ).extraMetadata );
    }
}
