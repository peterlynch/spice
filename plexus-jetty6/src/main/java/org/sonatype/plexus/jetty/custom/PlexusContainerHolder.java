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
package org.sonatype.plexus.jetty.custom;

import org.codehaus.plexus.PlexusContainer;
import org.sonatype.plexus.jetty.DefaultServletContainer;

/**
 * Manages a ThreadLocal, and makes the contained PlexusContainer available to Jetty LifeCycle.Listener
 * implementations. The ThreadLocal should be managed by whatever code initializes the Jetty Server instance, 
 * such as {@link DefaultServletContainer}.
 * 
 * @author jdcasey
 *
 */
public class PlexusContainerHolder
{
    
    private static ThreadLocal<PlexusContainer> containerLocal = new ThreadLocal<PlexusContainer>();
    
    public static void set( PlexusContainer container )
    {
        containerLocal.set( container );
    }
    
    public static PlexusContainer get()
    {
        return containerLocal.get();
    }

    public static void clear()
    {
        containerLocal.set( null );
    }

}
