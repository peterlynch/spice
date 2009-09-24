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
    public void generatePluginDescriptor( final PluginMetadataGenerationRequest request )
        throws GleanerException
    {
        // check request
        if ( request.getClassesDirectory() == null || !request.getClassesDirectory().exists() )
        {
            throw new GleanerException( "No classes to glean in directory: " + request.getClassesDirectory() );
        }

        if ( request.getClasspath() == null || request.getClasspath().isEmpty() )
        {
            throw new GleanerException( "No file on classpath, nothing to glean." );
        }

        // TODO Auto-generated method stub
        PluginMetadata pluginMetadata = new PluginMetadata();

        // put it to request
        request.setPluginMetadata( pluginMetadata );

        pluginMetadata.setGroupId( request.getGroupId() );
        pluginMetadata.setArtifactId( request.getArtifactId() );
        pluginMetadata.setVersion( request.getVersion() );
        pluginMetadata.setName( request.getName() );
        pluginMetadata.setDescription( request.getDescription() );
        pluginMetadata.setPluginSite( request.getPluginSiteURL() );

        pluginMetadata.setApplicationId( request.getApplicationId() );
        pluginMetadata.setApplicationEdition( request.getApplicationEdition() );
        pluginMetadata.setApplicationMinVersion( request.getApplicationMinVersion() );
        pluginMetadata.setApplicationMaxVersion( request.getApplicationMaxVersion() );

        // set the licenses
        if ( request.getLicenses() != null )
        {
            for ( Entry<String, String> licenseEntry : request.getLicenses().entrySet() )
            {
                PluginLicense license = new PluginLicense();
                license.setType( licenseEntry.getKey() );
                license.setUrl( licenseEntry.getValue() );
            }
        }

        // set the dependencies
        if ( request.getClasspathDependencies() != null )
        {
            for ( GAVCoordinate dependency : request.getClasspathDependencies() )
            {
                PluginDependency pluginDependency = new PluginDependency();
                pluginDependency.setGroupId( dependency.getGroupId() );
                pluginDependency.setArtifactId( dependency.getArtifactId() );
                pluginDependency.setVersion( dependency.getVersion() );
                pluginDependency.setType( dependency.getType() );

                if ( dependency.getClassifier() != null )
                {
                    pluginDependency.setClassifier( dependency.getClassifier() );
                }

                pluginMetadata.addClasspathDependency( pluginDependency );
            }
        }

        if ( request.getPluginDependencies() != null )
        {
            for ( GAVCoordinate dependency : request.getPluginDependencies() )
            {
                PluginDependency pluginDependency = new PluginDependency();
                pluginDependency.setGroupId( dependency.getGroupId() );
                pluginDependency.setArtifactId( dependency.getArtifactId() );
                pluginDependency.setVersion( dependency.getVersion() );

                pluginMetadata.addPluginDependency( pluginDependency );
            }
        }
        /*
         * // now for the fun part! glean the classes try { List<String> components = this.findComponents(
         * request.getClassesDirectory(), this.createClassLoader( request.getClasspath() ),
         * request.getAnnotationClasses() ); // FIXME, update model // pluginMetadata.setComponents(components); for (
         * String componentClass : components ) { pluginMetadata.addComponent( componentClass ); } } catch ( Exception e
         * ) { throw new GleanerException( "Failed to glean classes.", e ); }
         */
        if ( request.getOutputFile() != null )
        {
            // write file
            try
            {
                this.writePluginMetadata( pluginMetadata, request.getOutputFile() );
            }
            catch ( IOException e )
            {
                throw new GleanerException( "Failed to write plugin metadata to: " + request.getOutputFile(), e );
            }
        }

    }

    // TODO: remove this
    private List<String> findComponents( final File classesDirectory, final ClassLoader classLoader,
                                         final List<Class<?>> annotationClasses )
        throws GleanerException
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
            annotationProcessor.processClass( classFileName, classLoader, listenerMap, false );
        }

        return listener.getComponentClassNames();
    }

    // TODO: remove this
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
                // will not happen, File.toURI.toURL!
            }
        }

        URL[] urls = list.toArray( new URL[list.size()] );

        return new URLClassLoader( urls, ClassLoader.getSystemClassLoader() );
    }

    private void writePluginMetadata( final PluginMetadata pluginMetadata, final File outputFile )
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
