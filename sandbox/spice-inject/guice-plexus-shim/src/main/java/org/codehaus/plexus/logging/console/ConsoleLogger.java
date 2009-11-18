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
package org.codehaus.plexus.logging.console;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

public final class ConsoleLogger
    extends AbstractLogger
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String[] TAGS = { "[DEBUG] ", "[INFO] ", "[WARN] ", "[ERROR] ", "[FATAL] " };

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ConsoleLogger( final int threshold, @SuppressWarnings( "unused" ) final String name )
    {
        setThreshold( threshold );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Logger getChildLogger( final String name )
    {
        return this;
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

    @Override
    protected void log( final int level, final String message, final Throwable throwable )
    {
        System.out.println( TAGS[level].concat( message ) );
        if ( throwable != null )
        {
            throwable.printStackTrace( System.out );
        }
    }
}