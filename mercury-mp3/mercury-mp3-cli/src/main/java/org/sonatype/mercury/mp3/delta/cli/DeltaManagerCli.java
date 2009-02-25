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
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactQueryList;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.plexus.PlexusMercury;
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
import org.sonatype.mercury.mp3.delta.maven.MavenDeltaManager;

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
    private static  final Language LANG = new DefaultLanguage( DeltaManagerCli.class );
    
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

    private static final char CLOSURE = 'c';
    
    private static final char SHOW_LOCAL_SAVEPOINTS = 'l';
     
    private static final char SHOW_DETAILS = 'd';
    
    private static final char SHOW_GUI = 'g';
    
    private static final char NO_TEST_BINARIES = 'n';
    
    private static final char VERSION_REPO = 'r';
    
    private static final char HELP = 'h';
    
    private static final char _TEST = 't';
    
    private Options _options = new Options();

    private String _settings = DEFAULT_SETTINGS;
    
    private String _mavenHome;
    
    private String _cdUrl;
    
    private List<Repository> _repos;
    
    private List<Repository> _remoteRepos;
    
    private Monitor _monitor;
    
    private List<Artifact> _remoteVersions;
    
    private List<Artifact> _localVersions;
    
    private List<Artifact> _versions;
    
    private PlexusMercury _mercury;
    
    private boolean _showDetails = false;
    //--------------------------------------------------------------------------------------------------------
   public static void main( String[] args )
    throws Exception
    {
        new DeltaManagerCli().execute( args );
    }
   //--------------------------------------------------------------------------------------------------------
   @Override
    public void displayHelp()
    {
        System.out.println();

        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp( "mp3 options", "\noptions:", _options, "\n" );
    }
   //--------------------------------------------------------------------------------------------------------
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
                           OptionBuilder.withLongOpt( "closure" ).hasArg()
                           .withDescription( LANG.getMessage( "cli.closure" ) ).create( CLOSURE ) 
                        );
        _options.addOption( 
                           OptionBuilder.withLongOpt( "ver.repo" ).hasArg()
                           .withDescription( LANG.getMessage( "cli.ver.repo" ) ).create( VERSION_REPO ) 
                        );

        _options.addOption( 
                           OptionBuilder.withLongOpt( "no.test.binaries" )
                           .withDescription( LANG.getMessage( "cli.no.test.binaries" ) ).create( NO_TEST_BINARIES ) 
                        );
        
        _options.addOption( 
                           OptionBuilder.withLongOpt( "show.details" )
                           .withDescription( LANG.getMessage( "cli.show.details" ) ).create( SHOW_DETAILS ) 
                        );
        _options.addOption( 
                           OptionBuilder.withLongOpt( "show.local" )
                           .withDescription( LANG.getMessage( "cli.show.local.savepoints" ) ).create( SHOW_LOCAL_SAVEPOINTS ) 
                        );
        _options.addOption( 
                           OptionBuilder.withLongOpt( "show.gui" )
                           .withDescription( LANG.getMessage( "cli.show.gui" ) ).create( SHOW_GUI ) 
                        );
        _options.addOption( 
                           OptionBuilder.withLongOpt( "help" )
                           .withDescription( LANG.getMessage( "cli.help" ) ).create( HELP ) 
                        );
        
        
        
        _options.addOption( 
                           OptionBuilder.withLongOpt( "ztest" ).hasArg()
                           .withDescription( LANG.getMessage( "cli.test" ) ).create( _TEST ) 
                        );

       return _options;
    }
    //--------------------------------------------------------------------------------------------------------
    private void initMonitor( CommandLine cli )
    throws Exception
    {
        _showDetails = cli.hasOption( SHOW_DETAILS );

        String monitorClass = System.getProperty( SYSTEM_PROPERTY_MONITOR );
        
        if( monitorClass == null )
            _monitor = new DefaultMonitor(false);
        else
            _monitor = (Monitor) Class.forName( monitorClass ).newInstance();
    }
    //--------------------------------------------------------------------------------------------------------
    private void initSettings( CommandLine cli, Monitor monitor )
    throws Exception
    {
        if( cli.hasOption( SETTINGS ) )
            _settings = cli.getOptionValue( SETTINGS );
    }
    //--------------------------------------------------------------------------------------------------------
    private void initMavenHome( CommandLine cli, Monitor monitor )
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
        
        if( _showDetails )
            Util.say( LANG.getMessage( "maven.home", _mavenHome ), _monitor );
    }
    //--------------------------------------------------------------------------------------------------------
    private void initDefaults( CommandLine cli )
    throws Exception
    {
        initMonitor( cli );
        
        initMavenHome( cli, _monitor );
        
        initSettings( cli, _monitor );
        
        if( cli.hasOption( SETTINGS ) )
            _settings = cli.getOptionValue( SETTINGS );
        
        _cdUrl = cli.getOptionValue( CD_URL );
        
        File settingsFile = null;
        
        settingsFile = new File( _settings );

        setRepositories( cli, settingsFile, _monitor );
        
        if( Util.isEmpty( _cdUrl ) )
        {
            _versions = new ArrayList<Artifact>();
            
            _remoteVersions = _mercury.read( _remoteRepos, new ArtifactBasicMetadata(VERSIONS_GAV) );
            
            if( !Util.isEmpty( _remoteVersions ) )
            {
                Artifact ver = _remoteVersions.get( 0 );
                if( !Util.isEmpty( ver.getFile() ) )
                {
                 List<ArtifactBasicMetadata> verMds = CdUtil.getVersions( ver.getFile(), CdUtil.DEFAULT_SCOPE_NAME );
                 
                 _remoteVersions = _mercury.read( _remoteRepos, verMds );
                 
                 if( !Util.isEmpty( _remoteVersions ) )
                     _versions.addAll( _remoteVersions );
                }
            }
            
            if( cli.hasOption( SHOW_LOCAL_SAVEPOINTS ) )
            {
                _localVersions = getLocalVersions( new File(_mavenHome).getName() );
                
                if( !Util.isEmpty( _localVersions ) )
                    _versions.addAll( _localVersions );
            }
        }
    }
    //--------------------------------------------------------------------------------------------------------
    private List<Artifact> getLocalVersions( final String containerId )
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
                                          
                                          
                                          if( name.equals( MavenDeltaManager.DEFAULT_CONTAINER_ID + "." + DeltaManager.CD_EXT ) )
                                              return false;// current version
                                          
//                                          if( name.matches( ".*-[0-9]{14}\\."+DeltaManager.CD_EXT ) )
                                          if( name.endsWith( "."+DeltaManager.CD_EXT ) )
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
    
    synchronized protected void resumeYourself()
    {
        notify();
    }
    
    //--------------------------------------------------------------------------------------------------------
    synchronized private void selectCd( CommandLine cli, Monitor monitor )
    throws Exception
    {
        boolean remoteSection = true;

        int count = 1;
        
        String currentVer = getCurrentVersion();
        
        if( cli.hasOption( SHOW_GUI ) )
        {
            DataManagerGui gui = new DataManagerGui();
            gui._cli = this;
            gui._mavenHomeLabel.setText( _mavenHome );
            gui._mavenHomeField.setText( _mavenHome );
            
            gui._currentVersion.setText( currentVer );
            
            for( Artifact a : _versions )
            {
                String ver = a.getVersion();
                
                if( remoteSection && ver.endsWith( "."+DeltaManager.CD_EXT  ) )
                {
                    remoteSection = false;
                    
                    gui._cdModel.addElement( LANG.getMessage( "sel.files" ) );
                }
                
                gui._cdModel.addElement( (count++)+": "+a.getVersion() );
            }
          
          gui.pack();
          gui.setVisible( true );
          
            wait();
          
          int sel = gui._cdList.getSelectedIndex();
          gui.dispose();
            
          Artifact a = _versions.get( sel );
          
          System.out.println("\n"+LANG.getMessage( "you.selected", a.getVersion() )+"\n");
          
          _cdUrl = a.getFile().getAbsolutePath();
        }
        else
        {
            if( Util.isEmpty( _versions ) )
                throw new Exception(LANG.getMessage( "no.versions.to.select", _mavenHome ));

            int len = _versions.size();
            int reply = 0;
            String title = "\n"+LANG.getMessage( "sel.title", currentVer )+"\n";
            String prompt = "\n"+LANG.getMessage( "sel.prompt" );
            
            do 
            {
                System.out.println(title);
                
                for( Artifact a : _versions )
                {
                    String ver = a.getVersion();
                    
                    if( remoteSection && ver.endsWith( "."+DeltaManager.CD_EXT  ) )
                    {
                        remoteSection = false;
                        
                        System.out.println( LANG.getMessage( "sel.files" ) );
                    }
                    
                    System.out.println((count++)+": "+a.getVersion() );
                }
                
                System.out.print(prompt);
                
                String selection = cli.hasOption( _TEST ) 
                                ? cli.getOptionValue( _TEST ) 
                                : new jline.ConsoleReader().readLine()
                                ;
                
                reply = Integer.parseInt( selection );
            }
            while( reply < 1 || reply > len );

            Artifact a = _versions.get( reply-1 );
            
            System.out.println("\n"+LANG.getMessage( "you.selected", a.getVersion() )+"\n");
            
            _cdUrl = a.getFile().getAbsolutePath();
        }
    }
    //--------------------------------------------------------------------------------------------------------
    @Override
    public void invokePlexusComponent( CommandLine cli, PlexusContainer plexus )
    throws Exception
    {
        try
        {
            if( cli.hasOption( HELP ))
            {
                displayHelp();
                return;
            }
            
            _mercury = plexus.lookup( PlexusMercury.class );

            if( cli.hasOption( CLOSURE ))
            {
                closure( cli );
                return;
            }

            initDefaults( cli );
            
            if( Util.isEmpty( _cdUrl ) )
                selectCd( cli, _monitor );
            
            if( Util.isEmpty( _cdUrl ) )
                throw new Exception(LANG.getMessage( "cd.not.selected" ));
            
            InputStream cdStream = FileUtil.toStream( _cdUrl );
            
            if( cdStream == null )
                throw new Exception( LANG.getMessage( "cd.stream.is.null", cli.getOptionValue( CD_URL ) ) );

            File mavenHomeFile = new File( _mavenHome );
            
            applyConfiguration( mavenHomeFile, cdStream, plexus, _repos, _showDetails ? _monitor : null );
            
            Util.say( LANG.getMessage( "done" ), _monitor );
        }
        catch ( Exception e )
        {
            Util.say( LANG.getMessage( "error", e.getMessage() ), _monitor );
        }

    }
    
    protected void update( DataManagerGui gui )
    {
        gui.dispose();
        System.exit( 0 );
    }
    //--------------------------------------------------------------------------------------------------------
    private void applyConfiguration( File mavenHome, InputStream cdStream, PlexusContainer plexus, List<Repository> repos, Monitor monitor )
    throws Exception
    {
        NodeConfig cd = new ConfigurationDescriptorXpp3Reader().read( cdStream );
        
        if( cd == null )
            throw new Exception( LANG.getMessage( "cd.is.null" ) );
        
        cd.setConfigurationRoot( mavenHome.getParent() );
        
        String containerId = mavenHome.getName();
        
        ContainerConfig cc = CdUtil.findContainer( cd, "maven", null );
        
        cc.setConfigurationRoot( mavenHome.getCanonicalPath() );
        
        cc.setId( containerId );
        
        DeltaManager dm = plexus.lookup( DeltaManager.class, "maven" );
        
        dm.applyConfiguration( cd, repos, monitor );
    }
    //--------------------------------------------------------------------------------------------------------
    private String getCurrentVersion()
    throws Exception
    {
        File mh = new File( _mavenHome );
        
        String containerName = MavenDeltaManager.DEFAULT_CONTAINER_ID; // mh.getName();
        
        File currentCd = new File( mh, "/"+DeltaManager.CD_DIR + "/" + containerName+"."+DeltaManager.CD_EXT );
        
        String noVersion = LANG.getMessage( "no.current.version" );
        
       if( ! currentCd.exists() )
           return noVersion;
        
        NodeConfig cd = new ConfigurationDescriptorXpp3Reader().read( new FileInputStream(currentCd) );
        
        ContainerConfig cc = CdUtil.findContainer( cd, MavenDeltaManager.TYPE, null );
        
        if( cc != null )
            return cc.getVersion();
        
        return noVersion;
    }
    //--------------------------------------------------------------------------------------------------------
    private List<Repository> getDefaultRepositories( CommandLine cli, Monitor monitor, String local, String... remote )
    throws Exception
    {
        DependencyProcessor dp = new MavenDependencyProcessor();
        _repos = new ArrayList<Repository>(8);
        
        LocalRepositoryM2 lr = new LocalRepositoryM2( "local", new File(local), dp );
        _repos.add( lr );
        
        if( _showDetails )
            Util.say( LANG.getMessage( "local.repo", local ), monitor );
        
        int count = 0;
        
        _remoteRepos = new ArrayList<Repository>( remote.length );
        
        if( remote != null)
        {
            for( String r : remote )
            {
                Server server = new Server( "central"+(count++), new URL(r) );
                
                RemoteRepositoryM2 rr = new RemoteRepositoryM2( server, dp );
                
                _repos.add( rr );
                
                _remoteRepos.add( rr );
                
                if( _showDetails )
                    Util.say( LANG.getMessage( "remote.repo", r ), monitor );
            }
        }
        
        return _repos;
    }
    //--------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void setRepositories( CommandLine cli, File settingsFile, Monitor monitor )
    throws Exception
    {
        
        String versionsRepo = cli.hasOption( VERSION_REPO ) ? cli.getOptionValue( VERSION_REPO ) : null;
        
        if( settingsFile == null || ! settingsFile.exists() )
        {
            if( versionsRepo == null )
                getDefaultRepositories( cli, monitor, DEFAULT_LOCAL_REPO, DEFAULT_CENTRAL );
            else
                getDefaultRepositories( cli, monitor, DEFAULT_LOCAL_REPO, versionsRepo, DEFAULT_CENTRAL );
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
        
        if( versionsRepo != null)
            repoUrls.add( versionsRepo );
        
        for( Profile p : profiles )
        {
            List<org.apache.maven.settings.Repository> repositories = p.getRepositories();
                if( Util.isEmpty( repositories ) )
                    continue;

            for( org.apache.maven.settings.Repository r : repositories )
            {
                String layout = r.getLayout();
                
                if( layout == null || "default".equals( layout ) )
                    if( ! repoUrls.contains( r.getUrl() ) )
                        repoUrls.add( r.getUrl() );
            }
        }
        
        if( Util.isEmpty( repoUrls ))
            throw new Exception( LANG.getMessage( "no.repos", settingsFile.getCanonicalPath() ) );
        
        String local = Util.nvlS( settings.getLocalRepository(), DEFAULT_LOCAL_REPO );
        
        getDefaultRepositories( cli, monitor, local, repoUrls.toArray( new String[ repoUrls.size() ] ) );
    }
    //--------------------------------------------------------------------------------------------------------
    /**
     * @throws Exception 
     * 
     */
    private void closure( CommandLine cli ) throws Exception
    {
        boolean testBinaries = !cli.hasOption( NO_TEST_BINARIES );
        
        initMonitor( cli );
        initSettings( cli, _monitor );
        
        File settingsFile = null;
        
        settingsFile = new File( _settings );

        setRepositories( cli, settingsFile, _monitor );
        
        DependencyUtil du = new DependencyUtil( _mercury );
        
        String dep = cli.getOptionValue( CLOSURE );
        
        List<String> theRest = cli.getArgList();
        
        if( Util.isEmpty( theRest ) || theRest.size() < 3 )
        {
            _monitor.message( LANG.getMessage( "cli.closure.usage" ) );
            return;
        }
        
        String re = theRest.get( 0 );
        
        String verFrom = theRest.get( 1 );
        
        String verTo = theRest.get( 2 );
        
        List<ArtifactMetadata> deps = du.floatSnapshot( new ArtifactQueryList( dep )
                                                        , re
                                                        , verFrom
                                                        , verTo
                                                         , ArtifactScopeEnum.runtime, _repos, _monitor
                                                      );
        
        List<Artifact> binaries = null;
        
        if( testBinaries )
            _mercury.read( _repos, deps );
        
        if( ! Util.isEmpty( deps ) )
        {
            if( testBinaries && Util.isEmpty( binaries ) )
            {
                _monitor.message( LANG.getMessage( "no.binaries.for", deps.toString() ) );
                return;
            }

            if( testBinaries && binaries.size() != deps.size() )
            {
                List<ArtifactBasicMetadata> diff = (List<ArtifactBasicMetadata>) CdUtil.binDiff( deps, binaries);
                int diffSize = diff == null ? 0 : diff.size();
                
                _monitor.message( LANG.getMessage( "no.all.binaries.for", ""+binaries.size(), ""+deps.size()
                                                   , ""+diffSize, diff == null ? "[]" : diff.toString()
                                                 ) 
                                );
                return;
            }

            System.out.println( "<dependencies>" );
            
            for( ArtifactMetadata md : deps )
                System.out.println( "<dependency><name>"+md.toString()+"</name></dependency>" );
            
            System.out.println( "</dependencies>" );
        }
        else
            System.out.println( "No dependencies found" );
    }
    //--------------------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------
}