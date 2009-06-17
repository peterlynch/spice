package org.sonatype.plugin.metadata;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;

public class PluginMetadataGenerationRequest
{

    public String groupId;
    public String artifactId;
    public String version;
    public String name;
    public String description;
    public String pluginSiteURL;    
    
    public String applicationId;
    public String applicationEdition;
    public String applicationMinVersion;
    public String applicationMaxVersion;
    
    public List<Class<?>> annotationClasses = new ArrayList<Class<?>>();
    
    public Map<String, String> licenses = new HashMap<String, String>();
    
    public List<Dependency> classpathDependencies = new ArrayList<Dependency>();
    public List<Dependency> pluginDependencies = new ArrayList<Dependency>();

    /** The character encoding of the source files, may be {@code null} or empty to use platform's default encoding. */
    public String sourceEncoding;

    /** Classes to examine for annotations which are used to generate component metadata. */
    public File classesDirectory;  

    /** Supporting classpath required by class-based annotation processing. */
    public List<File> classpath = new ArrayList<File>();

    /** Output file for the final component descriptor. */
    public File outputFile;
    
}
