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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactQueryList;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.plexus.PlexusMercury;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.util.FileUtil;
import org.apache.maven.mercury.util.Monitor;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
import org.sonatype.mercury.mp3.api.CdUtil;
import org.sonatype.mercury.mp3.api.DeltaManager;
import org.sonatype.mercury.mp3.api.DeltaManagerException;
import org.sonatype.mercury.mp3.api.cd.ContainerConfig;
import org.sonatype.mercury.mp3.api.cd.DependencyConfig;
import org.sonatype.mercury.mp3.api.cd.NodeConfig;

/**
 * @author Oleg Gusakov
 * @version $Id$
 */
@Component( role = DeltaManager.class, hint = "maven" )
public class MavenDeltaManager
implements DeltaManager
{
    private static final Language LANG = new DefaultLanguage( MavenDeltaManager.class );

    public static final String TYPE = "maven";
    
    public static final String DEFAULT_CONTAINER_ID = "apache-maven";

    File _configurationRoot;

    @Requirement
    PlexusMercury _mercury;

    @SuppressWarnings("unchecked")
    public Collection<ContainerConfig> applyConfiguration( NodeConfig configuration
                                                           , List<Repository> repos
                                                           , Monitor monitor
                                                         )
        throws DeltaManagerException
    {
        if ( configuration == null )
            throw new DeltaManagerException( LANG.getMessage( "null.config" ) );

        if ( configuration.getConfigurationRoot() == null )
            throw new DeltaManagerException( LANG.getMessage( "null.config.root" ) );

        _configurationRoot = new File( configuration.getConfigurationRoot() );

        if ( !_configurationRoot.exists() )
            _configurationRoot.mkdirs();

        TreeSet<ContainerConfig> containers = new TreeSet<ContainerConfig>();

        List<ContainerConfig> cl = configuration.getContainers();

        if ( cl == null || cl.isEmpty() )
            throw new DeltaManagerException( LANG.getMessage( "null.config.containers" ) );

        for ( ContainerConfig cc : cl )
        {
            if ( TYPE.equals( cc.getType() ) )
                containers.add( cc );
        }

        if ( containers.isEmpty() )
            return containers;

        Collection<ContainerConfig> configuredContainers = new ArrayList<ContainerConfig>( containers.size() );

        for ( ContainerConfig cc : containers )
        {
            ContainerConfig resCc = adjust( cc, repos, configuration, monitor );

            if ( resCc != null )
                configuredContainers.add( resCc );
        }

        return configuredContainers;
    }

    @SuppressWarnings("unchecked")
    private ContainerConfig adjust( ContainerConfig cc
                                    , List<Repository> repos
                                    , NodeConfig configuration
                                    , Monitor monitor
                                  )
        throws DeltaManagerException
    {
        
        if( Util.isEmpty( cc.getId() ))
            throw new DeltaManagerException( LANG.getMessage( "cc.no.id" ) );
        
        String mavenHomeDir = cc.getConfigurationRoot();
        
        File mavenRoot = _configurationRoot;

        mavenRoot.mkdirs();
        
        String newVersion = cc.getVersion();
        
        if( Util.isEmpty( newVersion ))
            throw new DeltaManagerException( LANG.getMessage( "cc.no.version", cc.getId() ) );

        File mavenHome = Util.isEmpty( mavenHomeDir) ? new File( mavenRoot, cc.getId() ) : new File( mavenHomeDir );

        File cdFolder = new File( mavenRoot, cc.getId() + "/"+DeltaManager.CD_DIR+"/" );
        
        File cdFile = new File( cdFolder, DEFAULT_CONTAINER_ID + "." +DeltaManager.CD_EXT );

        File ldlFile = new File( cdFolder, DEFAULT_CONTAINER_ID + "-" + newVersion + "."+DeltaManager.LDL_EXT );
        
        Util.say( LANG.getMessage( "processing.container", cc.getId(), mavenRoot.getAbsolutePath() ) , monitor );
        
        if ( !cdFolder.exists() ) // new installation
        {
            DependencyConfig distroConf = cc.getDistribution();

            if ( distroConf == null || Util.isEmpty( distroConf.getName() ) )
                throw new DeltaManagerException( LANG.getMessage( "empty.container.distro", cc.getId() ) );

            ArtifactMetadata distroQuery = new ArtifactMetadata( distroConf.getName() );

            List<ArtifactMetadata> query = new ArrayList<ArtifactMetadata>( 1 );

            query.add( distroQuery );

            List<Artifact> artifacts = null;

            try
            {
                artifacts = _mercury.read( repos, query );
            }
            catch ( RepositoryException e )
            {
                throw new DeltaManagerException( e );
            }

            if ( Util.isEmpty( artifacts ) )
                throw new DeltaManagerException( LANG.getMessage( "no.container.zip", distroQuery.toString() ) );

            Artifact zipArtifact = artifacts.get( 0 );

            if ( zipArtifact == null )
                throw new DeltaManagerException( LANG.getMessage( "internal.error.no.artifact", distroQuery.toString() ) );

            File zip = zipArtifact.getFile();

            if ( Util.isEmpty( zip ) )
                throw new DeltaManagerException( LANG.getMessage( "container.zip.empty", "" + zip ) );

            try
            {
                FileUtil.unZip( new FileInputStream( zip ), mavenRoot );
                
                Util.say( LANG.getMessage( "new.install", cc.getId(), mavenHome.getAbsolutePath(), zip.getAbsolutePath() ) , monitor );
            }
            catch ( Exception e )
            {
                throw new DeltaManagerException( e );
            }
            
            // now write the ldl
            List<DependencyConfig> depList = cc.getDependencies();
            
            if( ! Util.isEmpty( depList ))
            {
                List<ArtifactMetadata> bmdList = CdUtil.toDepList( depList );
                
                List<ArtifactMetadata> deps;
                try
                {
                    deps = _mercury.resolve( repos, ArtifactScopeEnum.runtime, new ArtifactQueryList( bmdList ), null, null );
                    CdUtil.write( deps, ldlFile );
                }
                catch ( Exception e )
                {
                    throw new DeltaManagerException(e);
                }
                
            }
        }
        else  // delta management
        {
            Util.say( LANG.getMessage( "adjusting.install", cc.getId(), mavenHome.getAbsolutePath() ) , monitor );

            List<ArtifactMetadata> dependencies = CdUtil.toDepList( cc.getDependencies() );

            if ( Util.isEmpty( dependencies ) )
                throw new DeltaManagerException( LANG.getMessage( "no.new.deps", cc.getId() ) );

            NodeConfig oldConfig = null;

            try
            {
                oldConfig = CdUtil.read( cdFile );
            }
            catch ( Exception e )
            {
                throw new DeltaManagerException( e );
            }

            // TODO oleg 2009-01-28 a hack for maven single container to work container 
            ContainerConfig oldCc = CdUtil.findContainer( oldConfig, TYPE, null );
            
            List<ArtifactMetadata> oldDependencies = CdUtil.toDepList( oldCc.getDependencies() );
            
            String oldVersion = oldCc.getVersion();

            if ( Util.isEmpty( oldDependencies ) && Util.isEmpty( oldVersion ) )
                throw new DeltaManagerException( LANG.getMessage( "no.old.deps", cc.getId() ) );

            try
            {
                List<ArtifactMetadata> newRes =
                    _mercury.resolve( repos, ArtifactScopeEnum.runtime, new ArtifactQueryList( dependencies ), null, null );
                
                List<ArtifactMetadata> oldRes = null;
                
                File oldLdlFile = new File( cdFolder, DEFAULT_CONTAINER_ID + "-" + oldVersion + ".ldl" );
                
                if( Util.isEmpty( oldVersion ) || Util.isEmpty( oldLdlFile ) )
                {
                    oldRes = _mercury.resolve( repos, ArtifactScopeEnum.runtime, new ArtifactQueryList( oldDependencies ), null, null );
                    
                    Util.say( LANG.getMessage( "delta.no.version", cc.getId() ), monitor );
                }
                else
                {
                    oldRes = CdUtil.readLdl( oldLdlFile );

                    Util.say( LANG.getMessage( "read.old.version", oldVersion, oldLdlFile.getAbsolutePath() ), monitor );
                }

                List<ArtifactMetadata> deleteDiff = (List<ArtifactMetadata>) CdUtil.minus( oldRes, newRes );

                List<ArtifactMetadata> installDiff = (List<ArtifactMetadata>) CdUtil.minus( newRes, oldRes );

                List<Artifact> artifacts = null;

                if ( !Util.isEmpty( installDiff ) )
                {
                    artifacts = _mercury.read( repos, installDiff );
                    
                    if( Util.isEmpty( artifacts ) )
                        throw new DeltaManagerException( LANG.getMessage( "cannot.read.new.diff", installDiff.toString() ) );
                    
                    if( artifacts.size() != installDiff.size() )
                    {
                        List<ArtifactMetadata> notFound = (List<ArtifactMetadata>) CdUtil.binDiff( installDiff, artifacts );
                        int diffSize = notFound == null ? 0 : notFound.size();

                        throw new DeltaManagerException( 
           LANG.getMessage( "cannot.read.all.new.diff", ""+artifacts.size(), ""+installDiff.size()
                                            , ""+diffSize, notFound == null ? "[]" : notFound.toString() 
                          ) 
                                                       );
                    }
                    
                    for( Artifact a : artifacts )
                        if( Util.isEmpty( a.getFile() ) )
                            throw new DeltaManagerException( LANG.getMessage( "artifact.no.file", a.toString() ) );
                }

                if ( !Util.isEmpty( deleteDiff ) )
                    remove( mavenHome, deleteDiff, monitor );

                if ( !Util.isEmpty( artifacts ) )
                    install( mavenHome, artifacts, monitor );
                
                CdUtil.write( newRes, ldlFile );
            }
            catch ( Exception e )
            {
                throw new DeltaManagerException( e );
            }
        }

        try
        {
            cdFile.getParentFile().mkdirs();
            
            CdUtil.write( configuration, cdFile );
            
            // write version of it
            
            cdFile = new File( cdFolder, DEFAULT_CONTAINER_ID+"-"+newVersion+"." + DeltaManager.CD_EXT );
            
            CdUtil.write( configuration, cdFile );

            Util.say( LANG.getMessage( "write.descriptor", cc.getId(), cdFile.getAbsolutePath() ) , monitor );
        }
        catch ( Exception e )
        {
            throw new DeltaManagerException( e );
        }

        return cc;
    }

    /**
     * @param mavenHome
     * @param artifacts
     * @throws IOException
     */
    private void install( File mavenHome, List<Artifact> artifacts, Monitor monitor )
        throws IOException
    {
        File lib = new File( mavenHome, "lib" );

        for ( Artifact a : artifacts )
        {
            File binary = a.getFile();

            FileUtil.copy( binary, lib, false );
            Util.say( LANG.getMessage( "install.file", binary.getCanonicalFile().toString() ), monitor );
        }
    }

    /**
     * @param mavenHome
     * @param diff
     * @throws IOException
     */
    private void remove( File mavenHome, List<ArtifactMetadata> diff, Monitor monitor )
        throws IOException
    {
        File lib = new File( mavenHome, "lib" );

        for ( ArtifactMetadata bmd : diff )
        {
            File binary = new File( lib, bmd.getFileName() );

            FileUtil.delete( binary );
            Util.say( LANG.getMessage( "delete.file", binary.getCanonicalFile().toString() ), monitor );
        }
    }

    public String getContainerType()
    {
        return TYPE;
    }

}
