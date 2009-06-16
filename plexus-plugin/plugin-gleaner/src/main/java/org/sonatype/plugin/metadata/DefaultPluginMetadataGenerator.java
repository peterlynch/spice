package org.sonatype.plugin.metadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.plugin.metadata.gleaner.AnnotationListener;
import org.sonatype.plugin.metadata.gleaner.AnnotationProcessor;
import org.sonatype.plugin.metadata.gleaner.ComponentListCreatingAnnotationListener;
import org.sonatype.plugin.metadata.gleaner.DefaultAnnotationProcessor;
import org.sonatype.plugin.metadata.gleaner.GleanerException;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginLicense;
import org.sonatype.plugins.model.PluginMetadata;
import org.sonatype.plugins.model.io.xpp3.PluginModelXpp3Writer;

@Component( role = PluginMetadataGenerator.class )
public class DefaultPluginMetadataGenerator
    implements PluginMetadataGenerator
{

    private Logger logger = Logger.getLogger( this.getClass() );

    public void generatePluginDescriptor( PluginMetadataGenerationRequest request )
        throws GleanerException
    {
        // check request
        if ( request.classesDirectory == null || !request.classesDirectory.exists() )
        {
            throw new GleanerException( "No classes to glean in directory: " + request.classesDirectory );
        }

        if ( request.classpath == null || request.classpath.isEmpty() )
        {
            throw new GleanerException( "No file on classpath, nothing to glean. " );
        }

        // TODO Auto-generated method stub
        PluginMetadata pluginMetadata = new PluginMetadata();
        pluginMetadata.setGroupId( request.groupId );
        pluginMetadata.setArtifactId( request.artifactId );
        pluginMetadata.setVersion( request.version );
        pluginMetadata.setName( request.name );
        pluginMetadata.setDescription( request.description );
        pluginMetadata.setPluginSite( request.pluginSiteURL );

        pluginMetadata.setApplicationId( request.applicationId );
        pluginMetadata.setApplicationEdition( request.applicationEdition );
        pluginMetadata.setApplicationMinVersion( request.applicationMinVersion );
        pluginMetadata.setApplicationMaxVersion( request.applicationMaxVersion );

        // set the licenses
        if ( request.licenses != null )
        {
            for ( Entry<String, String> licenseEntry : request.licenses.entrySet() )
            {
                PluginLicense license = new PluginLicense();
                license.setType( licenseEntry.getKey() );
                license.setUrl( licenseEntry.getValue() );
            }
        }

        // set the dependencies
        if ( request.dependencies != null )
        {
            for ( Dependency dependency : request.dependencies )
            {
                PluginDependency pluginDependency = new PluginDependency();
                pluginDependency.setGroupId( dependency.getGroupId() );
                pluginDependency.setArtifactId( dependency.getArtifactId() );
                pluginDependency.setVersion( dependency.getVersion() );

                pluginMetadata.addClasspathDependency( pluginDependency );
            }
        }

        // now for the fun part! glean the classes
        try
        {
            List<String> components = this.findComponents( request.classesDirectory, this
                .createClassLoader( request.classpath ), request.annotationClasses );

            // FIXME, update model
            // pluginMetadata.setComponents(components);
            for ( String componentClass : components )
            {
                pluginMetadata.addComponent( componentClass );
            }
        }
        catch ( Exception e )
        {
            throw new GleanerException( "Failed to glean classes.", e );
        }

        // write file
        try
        {
            this.writePluginMetadata( pluginMetadata, request.outputFile );
        }
        catch ( IOException e )
        {
            throw new GleanerException( "Failed to write plugin metadata to: " + request.outputFile, e );
        }

    }

    private List<String> findComponents( File classesDirectory, ClassLoader classLoader, List<Class<?>> annotationClasses )
    {
        // this will find all the classes we want to glean
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( classesDirectory );
        scanner.addDefaultExcludes();
        scanner.setIncludes( new String[] { "**/*.class" } );
        scanner.scan();

        String[] classesToGlean = scanner.getIncludedFiles();
        
        AnnotationProcessor annotationProcessor = new DefaultAnnotationProcessor();
        ComponentListCreatingAnnotationListener listener = new ComponentListCreatingAnnotationListener();
        Map<Class<?>, AnnotationListener> listenerMap = new HashMap<Class<?>, AnnotationListener>();
        
        for ( Class<?> annotationClass : annotationClasses )
        {
            listenerMap.put( annotationClass, listener );
        }

        for ( String classFileName : classesToGlean )
        {
            try
            {
                annotationProcessor.processClass( classFileName, classLoader, listenerMap );
            }
            catch ( GleanerException e )
            {
                this.logger.error( "Error gleaning class: " + classFileName, e );
            }
        }

        return listener.getComponentClassNames();
    }

    private ClassLoader createClassLoader( final List<File> elements )
        throws Exception
    {
        List<URL> list = new ArrayList<URL>();

        // Add the projects dependencies
        for ( File file : elements )
        {
            try
            {
                list.add( file.toURI().toURL() );
            }
            catch ( MalformedURLException e )
            {
                throw new MojoExecutionException( "Invalid classpath entry: " + file, e );
            }
        }

        URL[] urls = list.toArray( new URL[list.size()] );

        return new URLClassLoader( urls, ClassLoader.getSystemClassLoader() );
    }

    private void writePluginMetadata( PluginMetadata pluginMetadata, File outputFile )
        throws IOException
    {
        // make sure the file's parent is created
       outputFile.getParentFile().mkdirs();

        FileWriter fileWriter = null;

        try
        {
            fileWriter = new FileWriter( outputFile );

            PluginModelXpp3Writer writer = new PluginModelXpp3Writer();
            writer.write( fileWriter, pluginMetadata );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }

    }

}
