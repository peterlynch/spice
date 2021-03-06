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
package org.sonatype.webdav.util;


/**
 * Simple utility module to make it easy to plug in the server identifier
 * when integrating Tomcat.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class ServerInfo
{
    // These need to be taken from a plexus configuration
    private static String serverInfo = "Sonatype Webdav";

    private static String serverBuilt = "Sonatype";

    private static String serverNumber = "1.0";

    public static String getServerInfo()
    {
        return ( serverInfo );
    }

    public static String getServerBuilt()
    {
        return ( serverBuilt );
    }

    public static String getServerNumber()
    {
        return ( serverNumber );
    }

    public static void main( String args[] )
    {
        System.out.println( "Server version: " + getServerInfo() );
        System.out.println( "Server built:   " + getServerBuilt() );
        System.out.println( "Server number:  " + getServerNumber() );
        System.out.println( "OS Name:        " + System.getProperty( "os.name" ) );
        System.out.println( "OS Version:     " + System.getProperty( "os.version" ) );
        System.out.println( "Architecture:   " + System.getProperty( "os.arch" ) );
        System.out.println( "JVM Version:    " + System.getProperty( "java.runtime.version" ) );
        System.out.println( "JVM Vendor:     " + System.getProperty( "java.vm.vendor" ) );
    }

}
