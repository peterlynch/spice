package org.sonatype.plugin.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.plugins.model.PluginMetadata;

/**
 * Request for generating plugin metadata.
 * 
 * @author toby
 */
public class PluginMetadataGenerationRequest
{
    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String description;

    private String pluginSiteURL;

    private String applicationId;

    private String applicationEdition;

    private String applicationMinVersion;

    private String applicationMaxVersion;
    
    private String scmVersion;
    
    private String scmTimestamp;

    private final List<Class<?>> annotationClasses = new ArrayList<Class<?>>();

    private final Map<String, String> licenses = new HashMap<String, String>();

    private final Set<GAVCoordinate> classpathDependencies = new HashSet<GAVCoordinate>();

    private final Set<GAVCoordinate> pluginDependencies = new HashSet<GAVCoordinate>();

    /** The character encoding of the source files, may be {@code null} or empty to use platform's default encoding. */
    private String sourceEncoding;

    /** Classes to examine for annotations which are used to generate component metadata. */
    private File classesDirectory;

    /** Supporting classpath required by class-based annotation processing. */
    private List<File> classpath = new ArrayList<File>();

    /** The resulting metadata */
    private PluginMetadata pluginMetadata;

    /** Output file for the final component descriptor. */
    private File outputFile;

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

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getPluginSiteURL()
    {
        return pluginSiteURL;
    }

    public void setPluginSiteURL( String pluginSiteURL )
    {
        this.pluginSiteURL = pluginSiteURL;
    }

    public String getApplicationId()
    {
        return applicationId;
    }

    public void setApplicationId( String applicationId )
    {
        this.applicationId = applicationId;
    }

    public String getApplicationEdition()
    {
        return applicationEdition;
    }

    public void setApplicationEdition( String applicationEdition )
    {
        this.applicationEdition = applicationEdition;
    }

    public String getApplicationMinVersion()
    {
        return applicationMinVersion;
    }

    public void setApplicationMinVersion( String applicationMinVersion )
    {
        this.applicationMinVersion = applicationMinVersion;
    }

    public String getApplicationMaxVersion()
    {
        return applicationMaxVersion;
    }

    public void setApplicationMaxVersion( String applicationMaxVersion )
    {
        this.applicationMaxVersion = applicationMaxVersion;
    }

    public List<Class<?>> getAnnotationClasses()
    {
        return annotationClasses;
    }

    public void addLicense( String type, String url )
    {
        getLicenses().put( type, url );
    }

    public Map<String, String> getLicenses()
    {
        return licenses;
    }

    public void addClasspathDependency( GAVCoordinate coordinate )
    {
        getClasspathDependencies().add( coordinate );
    }

    public void addClasspathDependency( String g, String a, String v )
    {
        getClasspathDependencies().add( new GAVCoordinate( g, a, v ) );
    }

    public void addClasspathDependency( String composite )
        throws IllegalArgumentException
    {
        getClasspathDependencies().add( new GAVCoordinate( composite ) );
    }

    public Set<GAVCoordinate> getClasspathDependencies()
    {
        return classpathDependencies;
    }

    public void addPluginDependency( GAVCoordinate coordinate )
    {
        getPluginDependencies().add( coordinate );
    }

    public void addPluginDependency( String g, String a, String v )
    {
        getPluginDependencies().add( new GAVCoordinate( g, a, v ) );
    }

    public void addPluginDependency( String composite )
        throws IllegalArgumentException
    {
        getPluginDependencies().add( new GAVCoordinate( composite ) );
    }

    public Set<GAVCoordinate> getPluginDependencies()
    {
        return pluginDependencies;
    }

    public String getSourceEncoding()
    {
        return sourceEncoding;
    }

    public void setSourceEncoding( String sourceEncoding )
    {
        this.sourceEncoding = sourceEncoding;
    }

    public File getClassesDirectory()
    {
        return classesDirectory;
    }

    public void setClassesDirectory( File classesDirectory )
    {
        this.classesDirectory = classesDirectory;
    }

    public List<File> getClasspath()
    {
        return classpath;
    }

    public void setClasspath( List<File> classpath )
    {
        this.classpath = classpath;
    }

    public PluginMetadata getPluginMetadata()
    {
        return pluginMetadata;
    }

    public void setPluginMetadata( PluginMetadata pluginMetadata )
    {
        this.pluginMetadata = pluginMetadata;
    }

    public File getOutputFile()
    {
        return outputFile;
    }

    public void setOutputFile( File outputFile )
    {
        this.outputFile = outputFile;
    }

    public String getScmVersion()
    {
        return scmVersion;
    }

    public void setScmVersion( String scmVersion )
    {
        this.scmVersion = scmVersion;
    }

    public String getScmTimestamp()
    {
        return scmTimestamp;
    }

    public void setScmTimestamp( String scmTimestamp )
    {
        this.scmTimestamp = scmTimestamp;
    }
}
