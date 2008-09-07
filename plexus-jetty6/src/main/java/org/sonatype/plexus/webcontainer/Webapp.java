/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
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
