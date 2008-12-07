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
