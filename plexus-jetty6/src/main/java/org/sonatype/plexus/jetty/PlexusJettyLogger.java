/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.plexus.jetty;

import org.codehaus.plexus.util.StringUtils;
import org.mortbay.log.Logger;

public class PlexusJettyLogger
    implements Logger
{

    private org.codehaus.plexus.logging.Logger logger;

    public PlexusJettyLogger( org.codehaus.plexus.logging.Logger logger )
    {
        super();
        this.logger = logger;
    }

    public Logger getLogger( String name )
    {
        return new PlexusJettyLogger( logger.getChildLogger( name ) );
    }

    public void warn( String msg, Throwable th )
    {
        logger.warn( msg, th );
    }

    public void warn( String msg, Object arg0, Object arg1 )
    {
        logger.warn( format( msg, arg0, arg1 ) );
    }

    public void info( String msg, Object arg0, Object arg1 )
    {
        logger.info( format( msg, arg0, arg1 ) );
    }

    public void debug( String msg, Throwable th )
    {
        logger.debug( msg, th );
    }

    public void debug( String msg, Object arg0, Object arg1 )
    {
        logger.debug( format( msg, arg0, arg1 ) );
    }

    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    public void setDebugEnabled( boolean enabled )
    {
        if ( enabled )
        {
            logger.setThreshold( org.codehaus.plexus.logging.Logger.LEVEL_DEBUG );
        }
        else
        {
            logger.setThreshold( org.codehaus.plexus.logging.Logger.LEVEL_INFO );
        }
    }

    protected String format( String msg, Object o1, Object o2 )
    {
        if ( o1 != null )
        {
            msg = StringUtils.replaceOnce( msg, "{}", o1.toString() );
        }
        if ( o2 != null )
        {
            msg = StringUtils.replaceOnce( msg, "{}", o1.toString() );
        }
        return msg;
    }

}
