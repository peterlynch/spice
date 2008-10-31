package org.sonatype.plexus.plugin.manager;

import java.util.List;

import org.apache.maven.mercury.artifact.Artifact;

public class PluginResolutionResult
{
    List<Artifact> artifacts;

    public List<Artifact> getArtifacts()
    {
        return artifacts;
    }

    public void setArtifacts( List<Artifact> artifacts )
    {
        this.artifacts = artifacts;
    }        
}
