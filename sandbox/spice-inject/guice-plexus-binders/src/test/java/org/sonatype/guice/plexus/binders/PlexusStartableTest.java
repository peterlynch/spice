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
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

public class PlexusStartableTest
    extends TestCase
{
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
                install( new PlexusBindingModule() );
            }
        } ).injectMembers( this );
    }

    @Component( role = Startable.class, hint = "default" )
    static class DefaultStartable
        implements Startable
    {
        static String state;

        public void start()
        {
            state = "STARTED";
        }

        public void stop()
        {
            state = "STOPPED";
        }
    }

    @Component( role = Startable.class, hint = "broken" )
    static class BrokenStartable
        implements Startable
    {
        public void start()
            throws StartingException
        {
            throw new StartingException( "oops" );
        }

        public void stop()
            throws StoppingException
        {
            throw new StoppingException( "oops" );
        }
    }

    public void testStartable()
        throws StoppingException
    {
        assertNull( DefaultStartable.state );
        injector.getInstance( DefaultStartable.class );
        assertEquals( "STARTED", DefaultStartable.state );
        injector.getInstance( Startable.class ).stop();
        assertEquals( "STOPPED", DefaultStartable.state );
    }

    public void testBrokenStartable()
        throws StoppingException
    {
        try
        {
            injector.getInstance( BrokenStartable.class );
            fail( "Expected ProvisionException" );
        }
        catch ( ProvisionException e )
        {
            System.out.println( e.toString() );
        }

        injector.getInstance( Startable.class ).stop();
    }
}
