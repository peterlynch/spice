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

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;

import com.google.inject.ProvisionException;
import com.google.inject.spi.InjectionListener;

/**
 * Guice {@link InjectionListener} that keeps track of {@link Startable} components.
 */
final class PlexusStartableListener
    implements InjectionListener<Startable>, Startable
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger( PlexusStartableListener.class.getName() );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Collection<Startable> activeComponents;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PlexusStartableListener()
    {
        activeComponents = new ArrayList<Startable>();
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void start()
    {
        // nothing to do at startup
    }

    public void afterInjection( Startable startable )
    {
        synchronized ( activeComponents )
        {
            // remember so we can stop later
            activeComponents.add( startable );
        }

        try
        {
            startable.start();
        }
        catch ( StartingException e )
        {
            throw new ProvisionException( "Unable to start: " + startable, e );
        }
    }

    public synchronized void stop()
    {
        synchronized ( activeComponents )
        {
            for ( Startable startable : activeComponents )
            {
                try
                {
                    startable.stop();
                }
                catch ( Throwable e )
                {
                    LOGGER.throwing( PlexusStartableListener.class.getName(), "stop", e );
                }
            }

            // can now forget them all
            activeComponents.clear();
        }
    }
}
