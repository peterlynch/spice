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

import java.util.Iterator;
import java.util.Properties;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;

public class WebappInfo
    extends JettyComponent
{
    private String warPath;

    private String contextPath;

    private Properties contextAttributes;

    public String getWarPath()
    {
        return warPath;
    }

    public void setWarPath( String warPath )
    {
        this.warPath = warPath;
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public void setContextPath( String contextPath )
    {
        this.contextPath = contextPath;
    }

    public Properties getContextAttributes()
    {
        return contextAttributes;
    }

    public void setContextAttributes( Properties contextAttributes )
    {
        this.contextAttributes = contextAttributes;
    }

    public WebAppContext getWebAppContext( Context context, ContextHandlerCollection parent )
        throws Exception
    {
        WebAppContext webapp = new WebAppContext( parent, getWarPath(), getContextPath() );

        // Put all our custom attribute into the servlet context

        if ( getContextAttributes() != null )
        {
            for ( Iterator<Object> i = getContextAttributes().keySet().iterator(); i.hasNext(); )
            {
                String attributeKey = (String) i.next();

                webapp.setAttribute( attributeKey, getContextAttributes().getProperty( attributeKey ) );
            }
        }

        // Put the container for the application into the servlet context

        PlexusContainer container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
        
        webapp.setAttribute( PlexusConstants.PLEXUS_KEY, container );

        webapp.setClassLoader( container.getContainerRealm() );

        return webapp;
    }
}
