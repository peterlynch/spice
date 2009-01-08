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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.plexus.PlexusMercury;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.util.FileUtil;
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
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
@Component( role=DeltaManager.class )
public class MavenDeltaManager
implements DeltaManager
{
    private static final Language LANG = new DefaultLanguage( MavenDeltaManager.class );
    
    public static final String TYPE = "maven";
    
    File _configurationRoot;
    
    @Requirement
    PlexusMercury _mercury;

    public Collection<ContainerConfig> applyConfiguration( NodeConfig configuration, List<Repository> repos )
    throws DeltaManagerException
    {
        if( configuration == null )
            throw new DeltaManagerException( LANG.getMessage( "null.config" ) );

        if( configuration.getConfigurationRoot() == null )
            throw new DeltaManagerException( LANG.getMessage( "null.config.root" ) );
        
        _configurationRoot = new File( configuration.getConfigurationRoot() );
        
        if( ! _configurationRoot.exists() )
            _configurationRoot.mkdirs();
        
        TreeSet<ContainerConfig> containers = new TreeSet<ContainerConfig>();
        
        List<ContainerConfig> cl = configuration.getContainers();
        
        if( cl == null || cl.isEmpty() )
            throw new DeltaManagerException( LANG.getMessage( "null.config.containers" ) );
        
        for( ContainerConfig cc : cl )
        {
            if( TYPE.equals( cc.getType() ) )
                containers.add( cc );
        }
        
        if( containers.isEmpty() )
            return containers;
        
        Collection<ContainerConfig> configuredContainers = new ArrayList<ContainerConfig>( containers.size() );

        for( ContainerConfig cc : containers )
        {
            ContainerConfig resCc = adjust( cc, repos, configuration );
            
            if( resCc != null )
                configuredContainers.add( resCc );
        }
        
        return configuredContainers;
    }
    
    private ContainerConfig adjust( ContainerConfig cc, List<Repository> repos,  NodeConfig configuration )
    throws DeltaManagerException
    {
        File mavenRoot = new File( _configurationRoot, "maven" );
        
        mavenRoot.mkdirs();
        
        File mavenHome = new File( mavenRoot, cc.getId() );
        
        if( !mavenHome.exists() ) // new installation
        {
            DependencyConfig distroConf = cc.getDistribution();

            if( distroConf == null || Util.isEmpty( distroConf.getName() ) )
                throw new DeltaManagerException( LANG.getMessage( "empty.container.distro", cc.getId()) );

            ArtifactMetadata distroQuery = new ArtifactMetadata( distroConf.getName() );

            List<ArtifactMetadata> query = new ArrayList<ArtifactMetadata>(1);

            query.add( distroQuery );

            List<Artifact> artifacts = null;

            try
            {
                artifacts = _mercury.read( repos, query );
            }
            catch ( RepositoryException e )
            {
                throw new DeltaManagerException(e);
            }
            
            if( Util.isEmpty( artifacts ) )
                throw new DeltaManagerException( LANG.getMessage( "no.container.zip",  distroQuery.toString() ) );
                
            Artifact zipArtifact = artifacts.get( 0 );
            
            if( zipArtifact == null )
                throw new DeltaManagerException( LANG.getMessage( "internal.error.no.artifact",  distroQuery.toString() ) );
            
            File zip = zipArtifact.getFile();
            
            if( Util.isEmpty( zip ) )
                throw new DeltaManagerException( LANG.getMessage( "container.zip.empty",  ""+zip ) );
            
            try
            {
                FileUtil.unZip( new FileInputStream(zip), mavenRoot );
            }
            catch ( Exception e )
            {
                throw new DeltaManagerException( e );
            }
        }
        else // delta management
        {
            List<ArtifactBasicMetadata> coord = CdUtil.toDepList( cc.getDependencies() );
            
            if( coord == null || coord.isEmpty() )
                throw new DeltaManagerException( LANG.getMessage( "empty.container.coord", cc.getId()) );
            
            throw new DeltaManagerException( "NOT IMPLEMENTED YET" );
        }
        
        try
        {
            CdUtil.write( configuration, new File( mavenRoot, cc.getId()+".cd") );
        }
        catch ( Exception e )
        {
            throw new DeltaManagerException( e );
        }
        
        return cc;
    }

    public String getContainerType()
    {
        return TYPE;
    }

}
