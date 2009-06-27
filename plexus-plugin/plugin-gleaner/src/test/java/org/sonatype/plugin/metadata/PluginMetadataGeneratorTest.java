package org.sonatype.plugin.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginMetadata;
import org.sonatype.plugins.model.io.xpp3.PluginModelXpp3Reader;

public class PluginMetadataGeneratorTest
    extends PlexusTestCase
{

    private PluginMetadataGenerationRequest getTestRequest()
    {
        PluginMetadataGenerationRequest request = new PluginMetadataGenerationRequest();
        request.setGroupId( "org.sonatype.nexus.plugins" );
        request.setArtifactId( "nexus-sample-plugin" );
        request.setVersion( "1.0.0-SNAPSHOT" );
        request.setName( "Nexus Sample Plugin" );
        request.setDescription( "Nexus Sample Plugin" );
        request.setPluginSiteURL( "http://nexus.sonatype.org/" );
        request.setApplicationId( "Nexus" );
        request.setApplicationEdition( "OSS" );
        request.setApplicationMinVersion( "1.4" );
        request.setApplicationMaxVersion( "1.4.6" );
        request.getLicenses().put( "GPL", "http://www.gnu.org/licenses/gpl.txt" );
        request.getLicenses().put( "ASF", "http://www.apache.org/licenses/LICENSE-2.0.txt" );

        request.addClasspathDependency( "org.sonatype.nexus.plugins:nexus-other-plugin:1.0.1" );
        request.addClasspathDependency( "org.your.com:some-other-lib:1.0.2" );

        request.setOutputFile( getTestFile( "target/testGenerator-plugin.xml" ) );
        request.setClassesDirectory( getTestFile( "target/test-classes" ) );
        request.getClasspath().add( getTestFile( "target/test-classes" ) );

        request.getAnnotationClasses().add( ExtensionPoint.class );
        request.getAnnotationClasses().add( Managed.class );

        return request;
    }

    public void testGenerator()
        throws Exception
    {
        PluginMetadataGenerator generater = (PluginMetadataGenerator) this.lookup( PluginMetadataGenerator.class );

        PluginMetadataGenerationRequest request = this.getTestRequest();

        generater.generatePluginDescriptor( request );

        // check the results
        verifyRequest( request );
    }

    private void verifyRequest( PluginMetadataGenerationRequest request )
        throws IOException, XmlPullParserException
    {
        File outputFile = request.getOutputFile();

        Assert.assertTrue( outputFile.exists() );

        PluginMetadata metadata = null;

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( outputFile );

            PluginModelXpp3Reader reader = new PluginModelXpp3Reader();
            metadata = reader.read( fis );
        }
        finally
        {
            IOUtil.close( fis );
        }

        Assert.assertEquals( request.getGroupId(), metadata.getGroupId() );

        Assert.assertEquals( request.getArtifactId(), metadata.getArtifactId() );
        Assert.assertEquals( request.getVersion(), metadata.getVersion() );
        Assert.assertEquals( request.getName(), metadata.getName() );
        Assert.assertEquals( request.getDescription(), metadata.getDescription() );
        Assert.assertEquals( request.getPluginSiteURL(), metadata.getPluginSite() );
        Assert.assertEquals( request.getApplicationId(), metadata.getApplicationId() );
        Assert.assertEquals( request.getApplicationEdition(), metadata.getApplicationEdition() );
        Assert.assertEquals( request.getApplicationMinVersion(), metadata.getApplicationMinVersion() );
        Assert.assertEquals( request.getApplicationMaxVersion(), metadata.getApplicationMaxVersion() );

        // request.licenses.put( "GPL", "http://www.gnu.org/licenses/gpl.txt" );
        // request.licenses.put( "ASF", "http://www.apache.org/licenses/LICENSE-2.0.txt" );

        Assert.assertEquals( request.getClasspathDependencies().size(), metadata.getClasspathDependencies().size() );

        for ( GAVCoordinate dependency : request.getClasspathDependencies() )
        {
            boolean found = false;

            for ( PluginDependency pluginDependency : (List<PluginDependency>) metadata.getClasspathDependencies() )
            {
                if ( dependency.getGroupId().equals( pluginDependency.getGroupId() )
                    && dependency.getArtifactId().equals( pluginDependency.getArtifactId() )
                    && dependency.getVersion().equals( pluginDependency.getVersion() ) )
                {
                    found = true;
                }
            }

            if ( !found )
            {
                Assert.fail( "failed to find dependency in result: " + dependency.getGroupId() + ":"
                    + dependency.getArtifactId() + ":" + dependency.getVersion() );
            }
        }

/*        Set<String> components = new HashSet<String>( metadata.getComponents() );

        Set<String> expectedComponents = new HashSet<String>();
        expectedComponents.add( "org.sonatype.plugin.test.ComponentExtentionPoint" );
        expectedComponents.add( "org.sonatype.plugin.test.ComponentManaged" );
        expectedComponents.add( "org.sonatype.plugin.test.ManagedViaInterface" );

        // now compare the lists
        Assert.assertEquals( expectedComponents, components );
*/
    }
}
