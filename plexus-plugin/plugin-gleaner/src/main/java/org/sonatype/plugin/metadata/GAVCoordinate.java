package org.sonatype.plugin.metadata;

import org.codehaus.plexus.util.StringUtils;

public class GAVCoordinate
{
    private static final String DEFAULT_TYPE = "jar";

    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String type = DEFAULT_TYPE;

    public GAVCoordinate()
    {
    }

    public GAVCoordinate( String groupId, String artifactId, String version )
    {
        setGroupId( groupId );

        setArtifactId( artifactId );

        setVersion( version );
    }

    public GAVCoordinate( String groupId, String artifactId, String version, String classifier, String type )
    {
        this( groupId, artifactId, version );

        if ( StringUtils.isNotBlank( classifier ) )
        {
            setClassifier( classifier );
        }

        if ( StringUtils.isNotBlank( type ) )
        {
            setType( type );
        }
    }

    public GAVCoordinate( String composite )
        throws IllegalArgumentException
    {
        if ( StringUtils.isBlank( composite ) )
        {
            throw new IllegalArgumentException( "The GAV composite form is bad: \"" + String.valueOf( composite )
                + "\", it must to conform to \"G:A:V[:[C][:T]]\"!" );
        }

        String[] parts = composite.split( ":" );

        if ( parts == null || parts.length < 3 )
        {
            throw new IllegalArgumentException( "The passed composite GAV coordinate is wrong: \"" + composite
                + "\" (It should have 3 parts, and sparated by \":\", example: \"groupId:artifactId:version\")." );
        }

        setGroupId( parts[0] );

        setArtifactId( parts[1] );

        setVersion( parts[2] );

        if ( parts.length >= 4 && StringUtils.isNotBlank( parts[3] ) )
        {
            setClassifier( parts[3] );
        }

        if ( parts.length >= 5 && StringUtils.isNotBlank( parts[4] ) )
        {
            setType( parts[4] );
        }
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

    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String toCompositeForm()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( String.valueOf( getGroupId() ) ).append( ":" ).append( String.valueOf( getArtifactId() ) )
            .append( ":" ).append( String.valueOf( getVersion() ) );

        if ( StringUtils.isNotBlank( getClassifier() ) )
        {
            sb.append( ":" ).append( getClassifier() );
        }

        if ( StringUtils.isNotBlank( getType() ) && !StringUtils.equals( DEFAULT_TYPE, getType() ) )
        {
            if ( StringUtils.isBlank( getClassifier() ) )
            {
                sb.append( ":" );
            }

            sb.append( ":" ).append( getType() );
        }

        return sb.toString();
    }

    // ==

    public String toString()
    {
        return toCompositeForm();
    }

    public int hashCode()
    {
        int hash = 7;

        hash = 31 * hash + ( getGroupId() != null ? getGroupId().hashCode() : 0 );

        hash = 31 * hash + ( getArtifactId() != null ? getArtifactId().hashCode() : 0 );

        hash = 31 * hash + ( getVersion() != null ? getVersion().hashCode() : 0 );

        hash = 31 * hash + ( getClassifier() != null ? getClassifier().hashCode() : 0 );

        hash = 31 * hash + ( getType() != null ? getType().hashCode() : 0 );

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
            && StringUtils.equals( getVersion(), other.getVersion() )
            && StringUtils.equals( getClassifier(), other.getClassifier() )
            && StringUtils.equals( getType(), other.getType() );
    }

}
