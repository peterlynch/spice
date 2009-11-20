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

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.sonatype.guice.plexus.config.PlexusConfigurator;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

public class PlexusConfigurationTest
    extends TestCase
{
    @Inject
    Component1 component1;

    @Inject
    Component2 component2;

    @Inject
    Injector injector;

    static class GlobalConfigurator
        implements PlexusConfigurator
    {
        @SuppressWarnings( "unchecked" )
        public <T> T configure( final Configuration config, final TypeLiteral<T> type )
        {
            return (T) ( "GLOBAL-" + config.name() + '-' + config.value() );
        }
    }

    static class LocalConfigurator
        implements PlexusConfigurator
    {
        @SuppressWarnings( "unchecked" )
        public <T> T configure( final Configuration config, final TypeLiteral<T> type )
        {
            return (T) ( "LOCAL-" + config.name() + '-' + config.value() );
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
                bind( PlexusConfigurator.class ).to( GlobalConfigurator.class );
                bind( Roles.configuratorKey( Object.class, "" ) ).to( LocalConfigurator.class );
                install( new PlexusBindingModule() );
            }
        } ).injectMembers( this );
    }

    @Component( role = Object.class )
    static class Component1
    {
        @Configuration( "unamed" )
        String a;

        @Configuration( name = "b", value = "named" )
        String field;
    }

    @Component( role = String.class )
    static class Component2
    {
        @Configuration( "unamed" )
        String a;

        @Configuration( name = "b", value = "named" )
        String field;
    }

    public void testLocalConfiguration()
    {
        assertEquals( "LOCAL-a-unamed", component1.a );
        assertEquals( "LOCAL-b-named", component1.field );
    }

    public void testGlobalConfiguration()
    {
        assertEquals( "GLOBAL-a-unamed", component2.a );
        assertEquals( "GLOBAL-b-named", component2.field );
    }

    public void testMissingConfiguration()
    {
        try
        {
            Guice.createInjector( new AbstractModule()
            {
                @Override
                protected void configure()
                {
                    install( new PlexusBindingModule() );
                }
            } ).getInstance( Component1.class );

            fail( "Expected error for missing configuration" );
        }
        catch ( final ProvisionException e )
        {
            System.out.println( e );
        }
    }
}
