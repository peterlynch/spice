package org.sonatype.plexus.plugin.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactMetadata;

public class PluginResolutionRequest
{
    private ArtifactMetadata artifactMetadata;

    private List<String> remoteRepositories;
          
    public ArtifactMetadata getArtifactMetadata()
    {
        return artifactMetadata;
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

    public PluginResolutionRequest()
    {
        remoteRepositories = new ArrayList<String>();
    }
    
    public PluginResolutionRequest addRemoteRepository( String remoteRepository )
    {
        remoteRepositories.add( remoteRepository );
        
        return this;
    }
    
    public List<String> getRemoteRepositories()
    {
        return remoteRepositories;
    }
}
