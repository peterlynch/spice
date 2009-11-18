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

public abstract class AbstractLogger
    implements Logger
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private int threshold = LEVEL_INFO;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final void debug( final String message )
    {
        if ( isDebugEnabled() )
        {
            log( LEVEL_DEBUG, message, null );
        }
    }

    public final void debug( final String message, final Throwable throwable )
    {
        if ( isDebugEnabled() )
        {
            log( LEVEL_DEBUG, message, throwable );
        }
    }

    public final boolean isDebugEnabled()
    {
        return threshold <= LEVEL_DEBUG;
    }

    public final void info( final String message )
    {
        if ( isInfoEnabled() )
        {
            log( LEVEL_INFO, message, null );
        }
    }

    public final void info( final String message, final Throwable throwable )
    {
        if ( isInfoEnabled() )
        {
            log( LEVEL_INFO, message, throwable );
        }
    }

    public final boolean isInfoEnabled()
    {
        return threshold <= LEVEL_INFO;
    }

    public final void warn( final String message )
    {
        if ( isWarnEnabled() )
        {
            log( LEVEL_WARN, message, null );
        }
    }

    public final void warn( final String message, final Throwable throwable )
    {
        if ( isWarnEnabled() )
        {
            log( LEVEL_WARN, message, throwable );
        }
    }

    public final boolean isWarnEnabled()
    {
        return threshold <= LEVEL_WARN;
    }

    public final void error( final String message )
    {
        if ( isErrorEnabled() )
        {
            log( LEVEL_ERROR, message, null );
        }
    }

    public final void error( final String message, final Throwable throwable )
    {
        if ( isErrorEnabled() )
        {
            log( LEVEL_ERROR, message, throwable );
        }
    }

    public final boolean isErrorEnabled()
    {
        return threshold <= LEVEL_ERROR;
    }

    public final void fatalError( final String message )
    {
        if ( isFatalErrorEnabled() )
        {
            log( LEVEL_FATAL, message, null );
        }
    }

    public final void fatalError( final String message, final Throwable throwable )
    {
        if ( isFatalErrorEnabled() )
        {
            log( LEVEL_FATAL, message, throwable );
        }
    }

    public final boolean isFatalErrorEnabled()
    {
        return threshold <= LEVEL_FATAL;
    }

    public final void setThreshold( final int theThreshold )
    {
        if ( theThreshold < LEVEL_DEBUG || theThreshold > LEVEL_DISABLED )
        {
            throw new IllegalArgumentException( "Invalid threshold: " + theThreshold );
        }
        threshold = theThreshold;
    }

    // ----------------------------------------------------------------------
    // Customizable methods
    // ----------------------------------------------------------------------

    protected abstract void log( int level, String message, Throwable throwable );
}
