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

package org.sonatype.mercury.mp3.delta.cli;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.maven.mercury.spi.http.server.HttpTestServer;
import org.apache.maven.mercury.util.FileUtil;

/**
 * @author Oleg Gusakov
 * @version $Id$
 */
public class DeltaManagerCliTest
extends TestCase
{
    File _configDir;

    static final String _remoteRepoDir = "./target/test-classes/repo";

    File _remoteRepoFile;

    static final String _remoteRepoUrlPrefix = "http://localhost:";

    static final String _remoteRepoUrlSufix = "/maven2";

    HttpTestServer _jetty;

    int _port;
    
    static final String _settings = "./target/test-classes/settings.xml";

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        _remoteRepoFile = new File( _remoteRepoDir );
        _jetty = new HttpTestServer( _remoteRepoFile, _remoteRepoUrlSufix );
        _jetty.start();
        _port = _jetty.getPort();
        
        replace( new File("./src/test/resources/settings.xml"), "${port}", ""+_port, new File(_settings) );

        _configDir = new File( "./target/config" );
    }
    
    private static void replace( File fFrom, String from, String to, File fTo )
    throws IOException
    {
        String s = FileUtil.readRawDataAsString( fFrom );
        s = s.replace( from, to );
        FileUtil.writeRawData( fTo, s );
    }

    @Override
    public void tearDown()
        throws Exception
    {
        super.tearDown();
        if ( _jetty != null )
        {
            _jetty.stop();
            _jetty.destroy();

            System.out.println( "Jetty on :" + _port + " destroyed\n<========\n\n" );
        }
    }
    
    public void testInstallNew()
    throws Exception
    {
        FileUtil.delete( _configDir );
        _configDir.mkdirs();
        
        File mavenCore1 = new File( _configDir, "apache-maven-3.0-alpha-1/lib/maven-core-3.0-alpha-1.jar" );
        File mavenCore2 = new File( _configDir, "apache-maven-3.0-alpha-1/lib/maven-core-3.0-alpha-2.jar" );

        assertFalse( mavenCore1.exists() );
        assertFalse( mavenCore2.exists() );
        
        DeltaManagerCli.main( new String [] { 
                                  "-m", "./target/config/apache-maven-3.0-alpha-1"
                                , "-u", "./target/test-classes/maven-3.0-alpha-1.cd" 
                                , "-s", "./target/test-classes/settings.xml" 
                                            }
                            );

        assertTrue(  mavenCore1.exists() );
        assertFalse( mavenCore2.exists() );
    }
    
    public void testInstallDelta()
    throws Exception
    {
        File mavenCore1 = new File( _configDir, "apache-maven-3.0-alpha-1/lib/maven-core-3.0-alpha-1.jar" );
        File mavenCore2 = new File( _configDir, "apache-maven-3.0-alpha-1/lib/maven-core-3.0-alpha-2.jar" );

        assertTrue( mavenCore1.exists() );
        assertFalse( mavenCore2.exists() );
        
        DeltaManagerCli.main( new String [] { 
                                  "-m", "./target/config/apache-maven-3.0-alpha-1"
                                , "-u", "./target/test-classes/maven-3.0-alpha-2.cd" 
                                , "-s", "./target/test-classes/settings.xml" 
                                            }
                            );

        assertFalse( mavenCore1.exists() );
        assertTrue(  mavenCore2.exists() );
    }

}
