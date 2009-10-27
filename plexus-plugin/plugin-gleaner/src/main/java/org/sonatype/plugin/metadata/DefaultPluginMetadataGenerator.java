package org.sonatype.plugin.metadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.plugin.metadata.gleaner.GleanerException;
import org.sonatype.plugins.model.ClasspathDependency;
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
        
        pluginMetadata.setScmVersion( request.getScmVersion() );

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
                ClasspathDependency pluginDependency = new ClasspathDependency();
                pluginDependency.setGroupId( dependency.getGroupId() );
                pluginDependency.setArtifactId( dependency.getArtifactId() );
                pluginDependency.setVersion( dependency.getVersion() );
                pluginDependency.setType( dependency.getType() );
                pluginDependency.setHasComponents( dependency.isHasComponents() );

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
