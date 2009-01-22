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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.artifact.version.MetadataVersionComparator;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.plexus.PlexusMercury;
import org.apache.maven.mercury.repository.api.ArtifactBasicResults;
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
    
    private static final String USER_HOME = System.getProperty( "user.home" );
    
    private static final String DEFAULT_SETTINGS = USER_HOME+"/.m2/settings.xml";
    
    public static final String SYSTEM_PROPERTY_DEFAULT_LOCAL_REPO = "maven.repo.local";
    private static final String DEFAULT_LOCAL_REPO = System.getProperty(   
                                                   SYSTEM_PROPERTY_DEFAULT_LOCAL_REPO
                                                 , USER_HOME+"/.m2/repository"
                                                                       );
    
    public static final String SYSTEM_PROPERTY_DEFAULT_CENTRAL = "maven.repo.central";
    private static final String DEFAULT_CENTRAL = System.getProperty( SYSTEM_PROPERTY_DEFAULT_CENTRAL
                                                                     , "http://repo1.maven.org/maven2" );
    
    private static final String VERSIONS_GAV = "org.apache.maven:maven-versions:1.0::ver";
    
    public static final String SYSTEM_PROPERTY_MONITOR = "mercury.monitor";
    
    private static final char MAVEN_HOME = 'm';

    private static final char CD_URL = 'u';

    private static final char SETTINGS = 's';
    
    private static final char SHOW_GUI = 'g';
    
    private Options _options = new Options();

    String _settings = DEFAULT_SETTINGS;
    
    String _mavenHome;
    
    String _cdUrl;
    
    List<Repository> _repos;
    
    List<Repository> _remoteRepos;
    
    Monitor _monitor;
    
    PlexusMercury _mercury;
    
    List<Artifact> _remoteVersions;
    
    List<Artifact> _localVersions;
    
    List<Artifact> _versions;

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
        _options.addOption( 
                           OptionBuilder.withLongOpt( "show.gui" )
                           .withDescription( LANG.getMessage( "cli.show.gui" ) ).create( SHOW_GUI ) 
                        );

       return _options;
    }
    
    private void initDefaults( CommandLine cli )
    throws Exception
    {
        File curF = new File(".");
        File mvn = new File( curF, "bin/mvn"+(File.pathSeparatorChar == ';'?".bat":"") );
        
        if( cli.hasOption( MAVEN_HOME ))
        {
            _mavenHome = cli.getOptionValue( MAVEN_HOME );
        }
        else if( mvn.exists() )
            _mavenHome = curF.getCanonicalPath();
        
        if( _mavenHome == null )
            throw new Exception( LANG.getMessage( "cli.no.maven.home", curF.getAbsolutePath(), MAVEN_HOME+"" ) );
        
        if( cli.hasOption( SETTINGS ) )
            _settings = cli.getOptionValue( SETTINGS );
        
        _cdUrl = cli.getOptionValue( CD_URL );

        String monitorClass = System.getProperty( SYSTEM_PROPERTY_MONITOR );
        
        if( monitorClass == null )
            _monitor = new DefaultMonitor();
        else
            _monitor = (Monitor) Class.forName( monitorClass ).newInstance();
        
        File settingsFile = null;
        
        settingsFile = new File( _settings );

        setRepositories( settingsFile, _monitor );
        
//        if( Util.isEmpty( _remoteRepos ) )
//            throw new Exception( LANG.getMessage( "no.remote.repos" ) );
        if( Util.isEmpty( _cdUrl ) )
        {
            _versions = new ArrayList<Artifact>();
            
            _remoteVersions = _mercury.read( _remoteRepos, new ArtifactBasicMetadata(VERSIONS_GAV) );
            
            if( !Util.isEmpty( _remoteVersions ) )
                _versions.addAll( _remoteVersions );
            
            _localVersions = getLocalVersions();
            
            if( !Util.isEmpty( _localVersions ) )
                _versions.addAll( _localVersions );
        }
    }
    
    
    private List<Artifact> getLocalVersions()
    {
        ArrayList<Artifact> res = new ArrayList<Artifact>(8);
        
        File cdDir = new File( _mavenHome, DeltaManager.CD_DIR );
        
        if( !cdDir.exists() )
            return res;
          
        File [] files = cdDir.listFiles( 
                                    new FileFilter()
                                    {
                                      public boolean accept( File pathname )
                                      {
                                          String name = pathname.getName();
                                          
                                          if( name.matches( ".*-[0-9]{14}\\."+DeltaManager.CD_EXT ) )
                                              return true;
                                          
                                          return false;
                                      }
                                     }
                                        );
          if( Util.isEmpty( files ) )
              return res;
          
          int count = files.length;
          
          TreeSet<String> sortedFiles = new TreeSet<String>();
          for( File f : files )
              sortedFiles.add( f.getName() );
          
          while( count-- > 0 )
          {
              String f = sortedFiles.last();
              
              DefaultArtifact da = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven:maven-cd:"+f) );
              
              da.setFile( new File( cdDir, f ) );
              
              res.add( da );
              
              sortedFiles.remove( f );
          }
          
          return res;
    }

    private void selectCd( CommandLine cli, Monitor monitor )
    throws Exception
    {
//        if( cli.hasOption( SHOW_GUI ) )
//        {
//            
//        }
//        else
        {
            if( Util.isEmpty( _versions ) )
                throw new Exception(LANG.getMessage( "no.versions.to.select", _mavenHome ));

            int len = _versions.size();
            int reply = 0;
            String title = "\n"+LANG.getMessage( "sel.title" )+"\n";
            String prompt = "\n"+LANG.getMessage( "sel.prompt" );
            
            do 
            {
                int count = 1;
                
                System.out.println(title);
                
                for( Artifact a : _versions )
                    System.out.println((count++)+": "+a.getVersion() );
                
                System.out.print(prompt);
                
                String selection = new jline.ConsoleReader().readLine();
                
                reply = Integer.parseInt( selection );
            }
            while( reply < 1 || reply > len );

            Artifact a = _versions.get( reply-1 );
            
            _cdUrl = a.getFile().getAbsolutePath();
        }
    }

    @Override
    public void invokePlexusComponent( CommandLine cli, PlexusContainer plexus )
    throws Exception
    {
        InputStream cdStream;
        
        _mercury = plexus.lookup( PlexusMercury.class );

        initDefaults( cli );
        
        if( Util.isEmpty( _cdUrl ) )
            selectCd( cli, _monitor );
        
        if( Util.isEmpty( _cdUrl ) )
            throw new Exception(LANG.getMessage( "cd.not.selected" ));
        
        cdStream = FileUtil.toStream( _cdUrl );
        
        if( cdStream == null )
            throw new Exception( LANG.getMessage( "cd.stream.is.null", cli.getOptionValue( CD_URL ) ) );

        File mavenHomeFile = new File( _mavenHome );
        
        applyConfiguration( mavenHomeFile, cdStream, plexus, _repos, _monitor );

    }
//        else
//        {
//            DataManagerGui gui = new DataManagerGui();
//            
//            gui._cli = this;
//            
//            initDefaults( cli );
//
//            gui._mavenHomeLabel.setText( _mavenHome );
//            gui._mavenHomeField.setText( gui._mavenHomeLabel.getText() );
//            
//            List<ArtifactBasicMetadata> versions = _mercury.readVersions( _repos, new ArtifactMetadata("org.apache.maven:maven-cd:(2.99,)::cd") );
//            
//            boolean versionsNotFound = true;
//            
//            if( !Util.isEmpty( versions ) )
//            {
//                TreeSet<ArtifactBasicMetadata> sortedVersions = new TreeSet<ArtifactBasicMetadata>( new MetadataVersionComparator() );
//
//                sortedVersions.addAll( versions );
//                versions.clear();
//                versions.addAll( sortedVersions );
//                Collections.reverse( versions );
//                
//                for( ArtifactBasicMetadata bmd : versions )
//                    gui._cdModel.addElement( bmd.getVersion() );
//
//                gui._cdList.setSelectedIndex( 1 );
//                
//                versionsNotFound = false;
//            }
//            
//            File cdDir = new File( _mavenHome, DeltaManager.CD_DIR );
//            if( cdDir.exists() )
//            {
//                File [] files = cdDir.listFiles( 
//                                          new FileFilter()
//                                          {
//                                            public boolean accept( File pathname )
//                                            {
//                                                String name = pathname.getName();
//                                                
//                                                if( name.matches( ".*-[0-9]{14}\\."+DeltaManager.CD_EXT ) )
//                                                    return true;
//                                                
//                                                return false;
//                                            }
//                                           }
//                                              );
//                if( !Util.isEmpty( files ) )
//                {
//                    int count = files.length;
//                    
//                    TreeSet<String> sortedFiles = new TreeSet<String>();
//                    for( File f : files )
//                        sortedFiles.add( f.getName() );
//                    
//                    while( count-- > 0 )
//                    {
//                        String v = sortedFiles.last();
//                        
//                        gui._tsModel.addElement( v );
//                        
//                        sortedFiles.remove( v );
//                    }
//                    
//                    if( versionsNotFound )
//                        gui._tsList.setSelectedIndex( 1 );
//                }
//            }
//            
//            DefaultComboBoxModel rModel = new DefaultComboBoxModel();
//            
//            for( Repository r : _repos )
//            {
//                if( r.isLocal() )
//                    gui._localRepoField.setText( r.getServer().getURL().toString() );
//                else
//                    rModel.addElement( r.getServer().getURL().toString() );
//            }
//            gui._remoteRepoList.setModel( rModel );
//            
//            gui.pack();
//            gui.setVisible( true );
//        }
//    }
    
    protected void update( DataManagerGui gui )
    {
//System.out.println(gui._mavenHomeLabel.getText() );
//System.out.println(gui._cdList.getSelectedIndex()+" : "+gui._cdList.getSelectedItem() );
        gui.dispose();
        System.exit( 0 );
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
        _repos = new ArrayList<Repository>(8);
        
        LocalRepositoryM2 lr = new LocalRepositoryM2( "local", new File(local), dp );
        _repos.add( lr );
        
        Util.say( LANG.getMessage( "local.repo", local ), monitor );
        
        int count = 0;
        
        if( remote != null)
        {
            _remoteRepos = new ArrayList<Repository>( remote.length );
            
            for( String r : remote )
            {
                Server server = new Server( "central"+(count++), new URL(r) );
                
                RemoteRepositoryM2 rr = new RemoteRepositoryM2( server, dp );
                
                _repos.add( rr );
                
                _remoteRepos.add( rr );
                
                Util.say( LANG.getMessage( "remote.repo", r ), monitor );
            }
        }
        
        return _repos;
    }

    @SuppressWarnings("unchecked")
    private void setRepositories( File settingsFile, Monitor monitor )
    throws Exception
    {
        if( settingsFile == null || ! settingsFile.exists() )
        {
            getDefaultRepositories( monitor, DEFAULT_LOCAL_REPO, DEFAULT_CENTRAL );
            return;
        }
        
        Settings settings = new SettingsXpp3Reader().read( new FileInputStream(settingsFile) );
        
        List<String> activeProfiles = new ArrayList<String>(8); 
        List<String> ap = settings.getActiveProfiles();
        
        if( Util.isEmpty( ap ) )
            throw new Exception( LANG.getMessage( "no.active.profiles", settingsFile.getCanonicalPath() ) );
        
        for( String apn : ap )
            activeProfiles.add( apn );
        
        List<Profile> profiles = settings.getProfiles();
        
        if( Util.isEmpty( profiles ) )
            throw new Exception( LANG.getMessage( "no.profiles", settingsFile.getCanonicalPath() ) );
        
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
            throw new Exception( LANG.getMessage( "no.repos", settingsFile.getCanonicalPath() ) );
        
        String local = Util.nvlS( settings.getLocalRepository(), DEFAULT_LOCAL_REPO );
        
        getDefaultRepositories( monitor, local, repoUrls.toArray( new String[ repoUrls.size() ] ) );
    }
    
}