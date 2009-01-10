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
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.DefaultMonitor;
import org.apache.maven.mercury.util.FileUtil;
import org.apache.maven.mercury.util.Monitor;
import org.apache.maven.mercury.util.Util;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
import org.codehaus.plexus.tools.cli.AbstractCli;
import org.sonatype.mercury.mp3.api.CdUtil;
import org.sonatype.mercury.mp3.api.DeltaManager;
import org.sonatype.mercury.mp3.api.cd.ContainerConfig;
import org.sonatype.mercury.mp3.api.cd.NodeConfig;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.ConfigurationDescriptorXpp3Reader;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class DeltaManagerCli
extends AbstractCli
{
    private static final Language LANG = new DefaultLanguage( DeltaManagerCli.class );
    
    private static final String DEFAULT_SETTINGS = System.getProperty( "user.home" )+"/.m2/settings.xml";
    
    private static final String SYSTEM_PROPERTY_DEFAULT_LOCAL_REPO = "maven.repo.local";
    private static final String DEFAULT_LOCAL_REPO = System.getProperty(   
                                                   SYSTEM_PROPERTY_DEFAULT_LOCAL_REPO
                                                 , System.getProperty( "user.home" )+"/.m2/repository"
                                                                       );
    
    private static final String SYSTEM_PROPERTY_DEFAULT_CENTRAL = "maven.repo.central";
    private static final String DEFAULT_CENTRAL = System.getProperty( SYSTEM_PROPERTY_DEFAULT_CENTRAL
                                                                     , "http://repo1.maven.org/maven2" );
    
    private static final char MAVEN_HOME = 'm';

    private static final char CD_URL = 'u';

    private static final char SETTINGS = 's';
    
    private Options _options = new Options();
    
   public static void main( String[] args )
    throws Exception
    {
        new DeltaManagerCli().execute( args );
    }

   @Override
    public void displayHelp()
    {
        System.out.println();

        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp( "mp3 options", "\noptions:", _options, "\n" );
    }
    
    @Override
    @SuppressWarnings( "static-access" )
    public Options buildCliOptions( Options someOptions )
    {
        
        _options.addOption( 
                          OptionBuilder.withLongOpt( "maven.home" ).hasArg()
                          .withDescription( LANG.getMessage( "cli.maven.home" ) ).create( MAVEN_HOME ) 
                       );
        _options.addOption( 
                          OptionBuilder.withLongOpt( "cd.url" ).hasArg()
                          .withDescription( LANG.getMessage( "cli.cd.url" ) ).create( CD_URL ) 
                       );
        _options.addOption( 
                          OptionBuilder.withLongOpt( "settings" ).hasArg()
                          .withDescription( LANG.getMessage( "cli.settings", DEFAULT_SETTINGS ) ).create( SETTINGS ) 
                       );

       return _options;
    }

    @Override
    public void invokePlexusComponent( CommandLine cli, PlexusContainer plexus )
    throws Exception
    {
        File mavenHome;
        
        InputStream cdStream;

        if ( cli.hasOption( MAVEN_HOME ) && cli.hasOption( CD_URL ) )
        {
            mavenHome = new File( cli.getOptionValue( MAVEN_HOME ) );
            
//            if( !mavenHome.exists() )
//                throw new Exception( LANG.getMessage( "maven.home.does.not.exist", cli.getOptionValue( MAVEN_HOME ) ) );
            
            cdStream = FileUtil.toStream( cli.getOptionValue( CD_URL ) );
            
            if( cdStream == null )
                throw new Exception( LANG.getMessage( "cd.stream.is.null", cli.getOptionValue( CD_URL ) ) );
            
            String settings = DEFAULT_SETTINGS;
            
            File settingsFile = null;
            
            if( cli.hasOption( SETTINGS ) )
            {
                settings = cli.getOptionValue( SETTINGS );
            }
            
            settingsFile = new File( settings );
                
            Monitor monitor = new DefaultMonitor();

            List<Repository> repos = getRepositories( settingsFile, monitor );
            
            applyConfiguration( mavenHome, cdStream, plexus, repos, monitor );
        }
        else
        {
            displayHelp();
        }
    }
    
    private void applyConfiguration( File mavenHome, InputStream cdStream, PlexusContainer plexus, List<Repository> repos, Monitor monitor )
    throws Exception
    {
        NodeConfig cd = new ConfigurationDescriptorXpp3Reader().read( cdStream );
        
        if( cd == null )
            throw new Exception( LANG.getMessage( "cd.is.null" ) );
        
        cd.setConfigurationRoot( mavenHome.getParent() );
        
        String containerId = mavenHome.getName();
        
        ContainerConfig cc = CdUtil.findContainer( cd, "maven", containerId );
        
        cc.setConfigurationRoot( mavenHome.getCanonicalPath() );
        
        DeltaManager dm = plexus.lookup( DeltaManager.class, "maven" );
        
        dm.applyConfiguration( cd, repos, monitor );
    }

    private List<Repository> getDefaultRepositories( Monitor monitor, String local, String... remote )
    throws Exception
    {
        DependencyProcessor dp = new MavenDependencyProcessor();
        List<Repository> repos = new ArrayList<Repository>(8);
        
        LocalRepositoryM2 lr = new LocalRepositoryM2( "local", new File(local), dp );
        repos.add( lr );
        
        Util.say( LANG.getMessage( "local.repo", local ), monitor );
        
        int count = 0;
        
        if( remote != null)
            for( String r : remote )
            {
                Server server = new Server( "central"+(count++), new URL(r) );
                
                RemoteRepositoryM2 rr = new RemoteRepositoryM2( server, dp );
                
                repos.add( rr );
                
                Util.say( LANG.getMessage( "remote.repo", r ), monitor );
            }
        
        return repos;
    }

    @SuppressWarnings("unchecked")
    private List<Repository> getRepositories( File settingsFile, Monitor monitor )
    throws Exception
    {
        if( settingsFile == null || ! settingsFile.exists() )
            return getDefaultRepositories( monitor, DEFAULT_LOCAL_REPO, DEFAULT_CENTRAL );
        
        Settings settings = new SettingsXpp3Reader().read( new FileInputStream(settingsFile) );
        
        List<String> activeProfiles = new ArrayList<String>(8); 
        List<String> ap = settings.getActiveProfiles();
        
        if( Util.isEmpty( ap ) )
        {
            Util.say( LANG.getMessage( "no.repos", settingsFile.getCanonicalPath() ), monitor );
            return null;
        }
        
        for( String apn : ap )
            activeProfiles.add( apn );
        
        List<Profile> profiles = settings.getProfiles();
        
        if( Util.isEmpty( profiles ) )
        {
            Util.say( LANG.getMessage( "no.repos", settingsFile.getCanonicalPath() ), monitor );
            return null;
        }
        
        List<String> repoUrls = new ArrayList<String>(8);
        
        for( Profile p : profiles )
        {
            List<org.apache.maven.settings.Repository> repositories = p.getRepositories();
                if( Util.isEmpty( repositories ) )
                    continue;

            for( org.apache.maven.settings.Repository r : repositories )
            {
                String layout = r.getLayout();
                
                if( layout == null || "default".equals( layout ) )
                    repoUrls.add( r.getUrl() );
            }
        }
        
        if( Util.isEmpty( repoUrls ))
        {
            Util.say( LANG.getMessage( "no.repos", settingsFile.getCanonicalPath() ), monitor );
            return null;
        }
        
        String local = Util.nvlS( settings.getLocalRepository(), DEFAULT_LOCAL_REPO );
        
        List<Repository> repos = getDefaultRepositories( monitor, local, repoUrls.toArray( new String[ repoUrls.size() ] ) );

        return repos;
    }
    
}
