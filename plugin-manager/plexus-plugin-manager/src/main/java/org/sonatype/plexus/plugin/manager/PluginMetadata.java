package org.sonatype.plexus.plugin.manager;

public class PluginMetadata
{
    private String groupId;
    private String artifactId;
    private String version;
    
    public PluginMetadata( String groupId, String artifactId, String version )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }
}
