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

package org.sonatype.mercury.configurator;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.maven.mercury.MavenDependencyProcessor;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.DefaultMonitor;
import org.apache.maven.mercury.util.Monitor;
import org.apache.maven.mercury.util.Util;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * @author Oleg Gusakov
 * @version $Id$
 */
@Component( role = MercuryConfigurator.class )
public class DefaultMercuryConfigurator
    implements MercuryConfigurator
{
    private static final Language LANG = new DefaultLanguage( DefaultMercuryConfigurator.class );

    private Monitor _monitor;
    
    private List<Repository> _repos;
    
    private boolean _showDetails = true;
    // --------------------------------------------------------------------------------------------------------
    @SuppressWarnings("static-access")
    public static Options getOptions()
    {
        Options opt = new Options();
        
        opt.addOption( 
                      OptionBuilder
                      .withLongOpt( OPTION_SETTINGS_LONG )
                      .hasArg()
                      .withDescription( LANG.getMessage( OPTION_MESSAGE_PREFIX+OPTION_SETTINGS_LONG, DEFAULT_SETTINGS ) )
                      .create( OPTION_SETTINGS ) 
                           );
        opt.addOption( 
                      OptionBuilder
                      .withLongOpt( OPTION_LOCAL_LONG )
                      .hasArg()
                      .withDescription( LANG.getMessage( OPTION_MESSAGE_PREFIX+OPTION_LOCAL_LONG, DEFAULT_LOCAL_REPO ) )
                      .create( OPTION_LOCAL ) 
                           );
        opt.addOption( 
                      OptionBuilder
                      .withLongOpt( OPTION_REMOTE_LONG )
                      .hasArg()
                      .withDescription( LANG.getMessage( OPTION_MESSAGE_PREFIX+OPTION_REMOTE_LONG, DEFAULT_REMOTE_REPO ) )
                      .create( OPTION_REMOTE ) 
                           );
        opt.addOption( 
               OptionBuilder
               .withLongOpt( OPTION_HELP_LONG )
               .withDescription( LANG.getMessage( OPTION_MESSAGE_PREFIX+OPTION_HELP_LONG ) )
               .create( OPTION_HELP ) 
                    );
        opt.addOption( 
               OptionBuilder
               .withLongOpt( OPTION_SHOW_DETAILS_LONG )
               .withDescription( LANG.getMessage( OPTION_MESSAGE_PREFIX+OPTION_SHOW_DETAILS_LONG ) )
               .create( OPTION_SHOW_DETAILS ) 
                    );
        opt.addOption( 
               OptionBuilder
               .withLongOpt( OPTION_OFFLINE_LONG )
               .withDescription( LANG.getMessage( OPTION_MESSAGE_PREFIX+OPTION_OFFLINE_LONG ) )
               .create( OPTION_OFFLINE ) 
                    );
        
        return opt;
    }

    // --------------------------------------------------------------------------------------------------------
    public Monitor getMonitor( CommandLine cli )
        throws MercuryConfiguratorException
    {
        try
        {
            String monitorClass = System.getProperty( SYSTEM_PROPERTY_MONITOR );

            if ( monitorClass == null )
                _monitor = new DefaultMonitor( false );
            else
                _monitor = (Monitor) Class.forName( monitorClass ).newInstance();

            return _monitor;
        }
        catch ( InstantiationException e )
        {
            throw new MercuryConfiguratorException( e );
        }
        catch ( IllegalAccessException e )
        {
            throw new MercuryConfiguratorException( e );
        }
        catch ( ClassNotFoundException e )
        {
            throw new MercuryConfiguratorException( e );
        }
    }

    public List<Repository> getRepositories( CommandLine cli )
        throws MercuryConfiguratorException
    {
        if( _repos != null )
            return _repos;
        
        try
        {
            setRepositories( cli, _monitor );
            
            return _repos;
        }
        catch ( Exception e )
        {
            throw new MercuryConfiguratorException( e );
        }
    }
    //--------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void setRepositories( CommandLine cli, Monitor monitor )
    throws Exception
    {
        boolean hasLocal = System.getProperties().contains( SYSTEM_PROPERTY_LOCAL_REPO );
        
        boolean hasRemote = System.getProperties().contains( SYSTEM_PROPERTY_REMOTE_REPO );
        
        if( ! cli.hasOption( OPTION_SETTINGS ) )
        {
            getDefaultRepositories( cli, monitor, DEFAULT_LOCAL_REPO, DEFAULT_REMOTE_REPO );
            return;
        }

        String settingsPath = DEFAULT_SETTINGS;
        
        if( cli.hasOption( OPTION_SETTINGS ) )
            settingsPath = cli.getOptionValue( OPTION_SETTINGS ).trim();
        
        File settingsFile = new File( settingsPath ); 
        
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
        
        if( !cli.hasOption( OPTION_OFFLINE ) )
        {
            if( hasRemote )
            {
                StringTokenizer st = new StringTokenizer( System.getProperty( SYSTEM_PROPERTY_REMOTE_REPO ), ",");
                
                while( st.hasMoreTokens() )
                    repoUrls.add( st.nextToken() );
            }
            else
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
        }
        
        if( Util.isEmpty( repoUrls ) )
            throw new Exception( LANG.getMessage( "no.repos", settingsFile.getCanonicalPath() ) );
        
        String local = hasLocal ? DEFAULT_LOCAL_REPO : Util.nvlS( settings.getLocalRepository(), DEFAULT_LOCAL_REPO );
        
        getDefaultRepositories( cli, monitor, local, repoUrls.toArray( new String[ repoUrls.size() ] ) );
    }
    //--------------------------------------------------------------------------------------------------------
    private List<Repository> getDefaultRepositories( CommandLine cli, Monitor monitor, String local, String... remote )
    throws Exception
    {
        DependencyProcessor dp = new MavenDependencyProcessor();
        _repos = new ArrayList<Repository>(8);
        
        LocalRepositoryM2 lr = new LocalRepositoryM2( new File(local), dp );
        _repos.add( lr );
        
        if( _showDetails )
            Util.say( LANG.getMessage( "local.repo", local ), monitor );
        
        if( remote != null)
        {
            for( String url: remote )
            {
                RemoteRepositoryM2 rr = new RemoteRepositoryM2( url, dp );
                
                _repos.add( rr );
                
                if( _showDetails )
                    Util.say( LANG.getMessage( "remote.repo", url),  monitor );
            }
        }
        
        return _repos;
    }
    //--------------------------------------------------------------------------------------------------------
}
