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
package org.sonatype.plexus.webcontainer;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;

public class Webapp
    extends JettyComponent
{
    private File warPath;
    private File webappDir;
    private String contextPath;
    private Properties contextAttributes;
    private boolean useParentLoader;
    
    public File getWarPath()
    {
        return warPath;
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public Properties getContextAttributes()
    {
        return contextAttributes;
    }

    public File getWebappDir()
    {
        return webappDir;
    }

    public boolean useParentLoader()
    {
        return useParentLoader;
    }        
}
