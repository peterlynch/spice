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

package org.sonatype.mercury.mp3.delta.maven;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.spi.http.server.HttpTestServer;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.DefaultMonitor;
import org.apache.maven.mercury.util.FileUtil;
import org.apache.maven.mercury.util.Monitor;
import org.apache.maven.mercury.util.TimeUtil;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.mercury.mp3.api.DeltaManager;
import org.sonatype.mercury.mp3.api.cd.ContainerConfig;
import org.sonatype.mercury.mp3.api.cd.DependencyConfig;
import org.sonatype.mercury.mp3.api.cd.NodeConfig;

/**
 * @author Oleg Gusakov
 * @version $Id$
 */
public class MavenDeltaManagerTest
    extends PlexusTestCase
{

    PlexusContainer _plexus;

    DeltaManager _mavenManager;

    List<Repository> _repos;

    File _localRepoDir;

    File _configDir;

    static final String _remoteRepoDir = "./target/test-classes/repo";

    File _remoteRepoFile;

    static final String _remoteRepoUrlPrefix = "http://localhost:";

    static final String _remoteRepoUrlSufix = "/maven2";

    HttpTestServer _jetty;

    int _port;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        
        _remoteRepoFile = new File( _remoteRepoDir );
        _jetty = new HttpTestServer( _remoteRepoFile, _remoteRepoUrlSufix );
        _jetty.start();
        _port = _jetty.getPort();

        _plexus = getContainer();

        _mavenManager = _plexus.lookup( DeltaManager.class, "maven" );

        DependencyProcessor dp = new MavenDependencyProcessor();

        Server server = new Server( "testRemote", new URL( _remoteRepoUrlPrefix + _port + _remoteRepoUrlSufix ) );

        RemoteRepositoryM2 central = new RemoteRepositoryM2( server, dp );

        _localRepoDir = new File( "./target/repo" );
        FileUtil.delete( _localRepoDir );
        _localRepoDir.mkdirs();

        server = new Server( "local", _localRepoDir.toURL() );

        LocalRepositoryM2 local = new LocalRepositoryM2( server, dp );

        _repos = new ArrayList<Repository>( 2 );

        _repos.add( local );
        _repos.add( central );

        _configDir = new File( "./target/config" );
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
    
    private NodeConfig install( String dep, String ver )
    throws Exception
    {
        DependencyConfig distroConf = new DependencyConfig();
        distroConf.setName( "org.apache.maven:maven-distribution:3.0-alpha-1:bin:zip" );

        DependencyConfig depConf = new DependencyConfig();
        depConf.setName( dep );

        ContainerConfig cc = new ContainerConfig();
        cc.setId( "apache-maven-3.0-alpha-1" );
        cc.setType( MavenDeltaManager.TYPE );
        cc.setDistribution( distroConf );
        cc.addDependency( depConf );
        if( ver != null )
            cc.setVersion( ver );

        NodeConfig config = new NodeConfig();
        config.setConfigurationRoot( _configDir.getCanonicalPath() );
        config.addContainer( cc );

        _mavenManager.applyConfiguration( config, _repos, new TestMonitor() );
        
        return config;
    }

    public void testInstallNew()
    throws Exception
    {
        FileUtil.delete( _configDir );
        _configDir.mkdirs();

        File mavenLib = new File( _configDir, "apache-maven-3.0-alpha-1/lib" );

        assertFalse( mavenLib.exists() );
        
        install( "org.apache.maven:maven-distribution:3.0-alpha-1", "3.0-alpha-1" );

        assertTrue( mavenLib.exists() );
        
        // need to wait for install timestamp to sttle in 
        Thread.sleep( 1000L );
    }

    public void testInstallDelta()
    throws Exception
    {
        File mavenLib = new File( _configDir, "apache-maven-3.0-alpha-1/lib" );
        assertTrue( mavenLib.exists() );
        
        // delete timestamped files
        File cd = new File( _configDir, "apache-maven-3.0-alpha-1/.cd" );
        
        File [] flist = cd.listFiles();
        for( File f : flist )
            if( f.getName().endsWith( ".ldl" ) )
                f.delete();
        
        File newArtifact = new File( _configDir, "apache-maven-3.0-alpha-1/lib/maven-core-3.0-alpha-2.jar" );

        assertFalse( newArtifact.exists() );
        
        assertFalse( verificationOk );
        
        setVerify( ", using it to read previous dependencies from" );
        
        install( "org.apache.maven:maven-distribution:3.0-alpha-2", "3.0-alpha-2" );

        assertTrue( mavenLib.exists() );

        assertTrue( newArtifact.exists() );
        
        assertFalse( verificationOk );
        
        setVerify( null );
        
        // need to wait for install timestamp to sttle in 
        Thread.sleep( 1000L );
    }

    public void testInstallDeltaTimestamp()
    throws Exception
    {
        String ts = TimeUtil.getUTCTimestamp();

        FileUtil.copy( new File( "./target/test-classes/apache-maven-3.0-alpha-1.ldl")
                     , new File("./target/config/apache-maven-3.0-alpha-1/.cd/apache-maven-3.0-alpha-1-"+ts+".ldl"), false );
        
        File newArtifact = new File( _configDir, "apache-maven-3.0-alpha-1/lib/maven-core-3.0-alpha-3.jar" );

        assertFalse( newArtifact.exists() );
        
        assertFalse( verificationOk );
        
        setVerify( "Current version .* dependencies read from .*" );
        
        install( "org.apache.maven:maven-distribution:3.0-alpha-3", ts );

        assertTrue( newArtifact.exists() );
        
        assertTrue( verificationOk );
    }
    
    String verification;
    boolean verificationOk = false;
    
    private void callback( String msg )
    {
        if( verification != null && !verificationOk )
            verificationOk = msg.matches( verification );
    }
    
    private void setVerify( String v )
    {
        this.verification = v;
        verificationOk = false;
    }

//===================================================================================================================

    class TestMonitor
    implements Monitor
    {
        Writer _writer;
        boolean _timestamp = true;
        private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
    
        public TestMonitor( boolean timestamp )
        {
            this( System.out );
            this._timestamp = timestamp;
        }
    
        public TestMonitor()
        {
            this( System.out );
        }
    
        public TestMonitor( OutputStream os )
        {
            _writer = new OutputStreamWriter( os );
        }
    
        public TestMonitor( Writer writer )
        {
            _writer = writer;
        }
    
        public void message( String msg )
        {
            callback( msg );
            
            try
            {
                if ( _writer != null )
                {
                    if( _timestamp )
                    {
                        _writer.write( fmt.format( new Date() ) );
                        _writer.write( ": " );
                    }
                    _writer.write( msg );
                    _writer.write( "\n" );
                    _writer.flush();
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    
    }

}
