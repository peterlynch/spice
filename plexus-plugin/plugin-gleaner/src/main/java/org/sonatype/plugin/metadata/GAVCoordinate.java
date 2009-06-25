package org.sonatype.plugin.metadata;

import org.codehaus.plexus.util.StringUtils;

public class GAVCoordinate
{
    private String groupId;

    private String artifactId;

    private String version;

    public GAVCoordinate()
    {
    }

    public GAVCoordinate( String groupId, String artifactId, String version )
    {
        setGroupId( groupId );

        setArtifactId( artifactId );

        setVersion( version );
    }

    public GAVCoordinate( String composite )
        throws IllegalArgumentException
    {
        String[] parts = StringUtils.split( composite, ":" );

        if ( parts == null || parts.length != 3 )
        {
            throw new IllegalArgumentException( "The passed composite GAV coordinate is wrong: \"" + composite
                + "\" (It should have 3 parts, and sparated by \":\", example: \"groupId:artifactId:version\")." );
        }

        setGroupId( parts[0] );

        setArtifactId( parts[1] );

        setVersion( parts[2] );
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    // ==

    public String toString()
    {
        return String.valueOf( getGroupId() ) + ":" + String.valueOf( getArtifactId() ) + ":"
            + String.valueOf( getVersion() );
    }

    public int hashCode()
    {
        int hash = 7;

        hash = 31 * hash + ( getGroupId() != null ? getGroupId().hashCode() : 0 );

        hash = 31 * hash + ( getArtifactId() != null ? getArtifactId().hashCode() : 0 );

        hash = 31 * hash + ( getVersion() != null ? getVersion().hashCode() : 0 );

        return hash;
    }

    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( ( obj == null ) || ( obj.getClass() != this.getClass() ) )
        {
            return false;
        }

        GAVCoordinate other = (GAVCoordinate) obj;

        return StringUtils.equals( getGroupId(), other.getGroupId() )
            && StringUtils.equals( getArtifactId(), other.getArtifactId() )
            && StringUtils.equals( getVersion(), other.getVersion() );
    }

}
