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
package org.codehaus.plexus.logging;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.guice.plexus.config.Roles;

public abstract class BaseLoggerManager
    implements LoggerManager, Initializable
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String threshold;

    private int currentThreshold;

    private final Map<String, Logger> componentLoggers = new HashMap<String, Logger>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void initialize()
    {
        currentThreshold = parseThreshold( threshold );
    }

    public final void setThresholds( final int currentThreshold )
    {
        this.currentThreshold = currentThreshold;
        for ( final Logger logger : componentLoggers.values() )
        {
            logger.setThreshold( currentThreshold );
        }
    }

    public final Logger getLoggerForComponent( final String role, final String hint )
    {
        final String key = Roles.canonicalRoleHint( role, hint );
        Logger logger = componentLoggers.get( key );
        if ( null == logger )
        {
            logger = createLogger( key );
            logger.setThreshold( currentThreshold );
            componentLoggers.put( key, logger );
        }
        return logger;
    }

    public final void returnComponentLogger( final String role, final String hint )
    {
        componentLoggers.remove( Roles.canonicalRoleHint( role, hint ) );
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected abstract Logger createLogger( String key );

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static final int parseThreshold( final String text )
    {
        if ( "DEBUG".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_DEBUG;
        }
        else if ( "INFO".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_INFO;
        }
        else if ( "WARN".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_WARN;
        }
        else if ( "ERROR".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_ERROR;
        }
        else if ( "FATAL".equalsIgnoreCase( text ) )
        {
            return Logger.LEVEL_FATAL;
        }

        return Logger.LEVEL_DEBUG;
    }
}
