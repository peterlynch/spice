/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.plexus.plugin.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactMetadata;

public class PluginResolutionRequest
{
    /** 
     * The metadata for the originating artifact. 
     */
    private ArtifactMetadata artifactMetadata;
    /**
     * The local repositories that will be searched for artifact metadata and artifacts.
     */
    private List<File> localRepositories;
    /**
     * The remote repositories that will be searched for artifact metadata and artifacts.
     */
    private List<String> remoteRepositories;

    public PluginResolutionRequest()
    {
        localRepositories = new ArrayList<File>();
        remoteRepositories = new ArrayList<String>();
    }
    
    public ArtifactMetadata getArtifactMetadata()
    {
        return artifactMetadata;
    }

    public PluginResolutionRequest setPluginMetadata( PluginMetadata pm )
    {
        this.artifactMetadata = new ArtifactMetadata( pm.getGroupId() + ":" + pm.getArtifactId() + ":" + pm.getVersion() );
        return this;
    }
    
    public PluginResolutionRequest setArtifactMetadata( String artifactMetadata )
    {
        this.artifactMetadata = new ArtifactMetadata( artifactMetadata );        
        return this;
    }

    public PluginResolutionRequest setArtifactMetadata( ArtifactMetadata artifactMetadata )
    {
        this.artifactMetadata = artifactMetadata;        
        return this;
    }
        
    public List<File> getLocalRepositories()
    {
        return localRepositories;
    }

    public PluginResolutionRequest addLocalRepository( File localRepository )
    {
        localRepositories.add( localRepository );
        return this;
    }

    public PluginResolutionRequest addRemoteRepository( String remoteRepository )
    {
        remoteRepositories.add( remoteRepository );        
        return this;
    }
    
    public PluginResolutionRequest setRemoteRepositories( List<String> remoteRepositories )    
    {
        this.remoteRepositories = remoteRepositories;
        return this;        
    }
    
    public List<String> getRemoteRepositories()
    {
        return remoteRepositories;
    }
}
