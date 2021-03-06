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

package org.sonatype.mercury.gav;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactQueryList;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.plexus.PlexusMercury;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.util.Monitor;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
import org.codehaus.plexus.tools.cli.AbstractCli;
import org.sonatype.mercury.configurator.DefaultMercuryConfigurator;
import org.sonatype.mercury.configurator.MercuryConfigurator;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryGavCli
extends AbstractCli
{
    private static  final Language LANG = new DefaultLanguage( MercuryGavCli.class );
    
    private static final String USER_HOME = System.getProperty( "user.home" );
    
    private static final String DEFAULT_SETTINGS = USER_HOME+"/.m2/settings.xml";
    
    public static final String SYSTEM_PROPERTY_MONITOR = "mercury.monitor";
    
    private static final char OPTION_LOCAL_REPO = 'l';
    
    private static final char OPTION_REMOTE_REPO = 'r';
    
    private static final char SETTINGS = 's';
    
    private static final char VERBOSE = 'v';
    
    @Requirement
    private PlexusMercury _mercury;
    
    private Monitor _monitor;
    
    private boolean _showDetails = false;
    
    private String _executableGAV;
    
    private String _executableClass;
    
    private List<Repository> _repos;
    
    private MercuryConfigurator _mc;
    
    private Options _options = new Options();
    
    private static List<String> _gavArgs;
    
    private static List<String> _mainArgs;
    //--------------------------------------------------------------------------------------------------------
    public static void main( String[] args )
     throws Exception
     {
        if( args == null || args.length < 1 )
        {
            usage();
            return;
        }
        
        boolean flip = false;
        
        _gavArgs = new ArrayList<String>( args.length );
        
        _mainArgs = new ArrayList<String>( args.length );
   
        for( String a : args )
        {
            if( a.indexOf( ':' ) != -1 )
                flip = true;
            
            if( flip )
                _mainArgs.add( a );
            else
                _gavArgs.add( a );
        }
        
         new MercuryGavCli().execute( _gavArgs.toArray( new String[ _gavArgs.size() ] ) );
     }
    //--------------------------------------------------------------------------------------------------------
     public static void usage()
     {
         System.out.println();

         System.out.println(LANG.getMessage( "cli.usage" ));
     }
    //--------------------------------------------------------------------------------------------------------
    @Override
     public void displayHelp()
     {
         System.out.println();

         HelpFormatter formatter = new HelpFormatter();

         formatter.printHelp( LANG.getMessage( "cli.usage" ), "\noptions:", _options, "\n" );
     }
    //--------------------------------------------------------------------------------------------------------
     @Override
     public Options buildCliOptions( Options someOptions )
     {
         _options = DefaultMercuryConfigurator.getOptions();
         
         return _options;
     }

    @SuppressWarnings("unchecked")
    @Override
    public void invokePlexusComponent( CommandLine cli, PlexusContainer plexus )
        throws Exception
    {
        try
        {
            _mc = plexus.lookup( MercuryConfigurator.class );
            
            _monitor = _mc.getMonitor( cli );
            
            if( cli.hasOption( HELP ))
            {
                displayHelp();

                return;
            }
            
            _repos = _mc.getRepositories( cli );

            if( Util.isEmpty( _mainArgs ) || _mainArgs.size() < 2 )
            {
                displayHelp();

                return;
            }
            
            _executableGAV = _mainArgs.get( 0 );
            
            _executableClass = _mainArgs.get( 1 );
            
            ArtifactQueryList list = new ArtifactQueryList( new ArtifactMetadata(_executableGAV) );

            _mercury = plexus.lookup( PlexusMercury.class );

            List<ArtifactMetadata> deps = _mercury.resolve( _repos, ArtifactScopeEnum.runtime, list, null, null );
            
            if( deps == null )
                throw new Exception(LANG.getMessage( "cli.no.dependencies", _executableGAV ));
                
            for( ArtifactMetadata md : deps )
                _monitor.message( md.toString() );
            
            _monitor.message( "reading .." );

            List<Artifact> al = _mercury.read( _repos, deps );
            
            if( al == null )
                throw new Exception(LANG.getMessage( "cli.no.artifacts", _executableGAV ));
            
            if( deps.size() != al.size() )
                throw new Exception(LANG.getMessage( "cli.len.artifacts", _executableGAV, ""+deps.size(), ""+al.size() ));
            
            URL [] artifacts = new URL[ al.size() ]; 
            
            int count = 0;
            
            for( Artifact a : al )
                artifacts[ count++ ] = a.getFile().toURL();
            
            URLClassLoader cl = new URLClassLoader( artifacts );
            
            Class mainClass = cl.loadClass( _executableClass );
            
            Method [] methods = mainClass.getDeclaredMethods();
            
            Method mainMethod = null;
            
            for( Method m : methods )
                if( "main".equals( m.getName() ) )
                {
                    mainMethod = m;
                    
                    break;
                }
            
            if( mainMethod == null )
                throw new Exception(LANG.getMessage( "cli.no.main", _executableClass ));
            
            String [] args = null;
            
            if( _mainArgs.size() > 2 )
            {
                args = new String[ _mainArgs.size() - 2 ];
                
                for( int i=2; i < _mainArgs.size(); i++ )
                    args[i-2] = _mainArgs.get( i ); 
            }
            
            @SuppressWarnings("unused")
            Class [] params = mainMethod.getParameterTypes();
            
            mainMethod.invoke( null, (Object)args );
                    
        }
        catch ( Exception e )
        {
            Util.say( LANG.getMessage( "cli.error", e.getClass().getName(), e.getMessage() ), _monitor );
//            e.printStackTrace();
        }
        
        _monitor.message( LANG.getMessage( "cli.done" ) );
    }

}
