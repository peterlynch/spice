package org.sonatype.plugin.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.maven.model.Dependency;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.plugins.mock.MockExtensionPoint;
import org.sonatype.plugins.mock.MockManaged;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginMetadata;
import org.sonatype.plugins.model.io.xpp3.PluginModelXpp3Reader;

public class PluginMetadataGeneratorTest
    extends PlexusTestCase
{

    private PluginMetadataGenerationRequest getTestRequest()
    {
        PluginMetadataGenerationRequest request = new PluginMetadataGenerationRequest();
        request.groupId = "org.sonatype.nexus.plugins";
        request.artifactId = "nexus-sample-plugin";
        request.version = "1.0.0-SNAPSHOT";
        request.name = "Nexus Sample Plugin";
        request.description = "Nexus Sample Plugin";
        request.pluginSiteURL = "http://nexus.sonatype.org/";
        request.applicationId = "Nexus";
        request.applicationEdition = "OSS";
        request.applicationMinVersion = "1.4";
        request.applicationMaxVersion = "1.4.6";
        request.licenses.put( "GPL", "http://www.gnu.org/licenses/gpl.txt" );
        request.licenses.put( "ASF", "http://www.apache.org/licenses/LICENSE-2.0.txt" );

        Dependency dependency1 = new Dependency();
        dependency1.setGroupId( "org.sonatype.nexus.plugins" );
        dependency1.setArtifactId( "nexus-other-plugin" );
        dependency1.setVersion( "1.0.1" );
        request.classpathDependencies.add( dependency1 );

        Dependency dependency2 = new Dependency();
        dependency2.setGroupId( "org.your.com" );
        dependency2.setArtifactId( "some-other-lib" );
        dependency2.setVersion( "1.0.2" );
        request.classpathDependencies.add( dependency2 );

        request.outputFile = new File( "target/testGenerator-plugin.xml" );
        request.classesDirectory = new File( "target/test-classes" );
        request.classpath.add( new File( "target/test-classes" ) );

        request.annotationClasses.add( MockExtensionPoint.class );
        request.annotationClasses.add( MockManaged.class );

        return request;
    }

    public void testGenerator()
        throws Exception
    {

        PluginMetadataGenerator generater = (PluginMetadataGenerator) this.lookup( PluginMetadataGenerator.class );

        PluginMetadataGenerationRequest request = this.getTestRequest();

        generater.generatePluginDescriptor( request );

        // check the results
        this.verifyRequest( request );
    }

    private void verifyRequest( PluginMetadataGenerationRequest request )
        throws IOException,
            XmlPullParserException
    {
        File outputFile = request.outputFile;
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

        Assert.assertEquals( request.groupId, metadata.getGroupId() );

        Assert.assertEquals( request.artifactId, metadata.getArtifactId() );
        Assert.assertEquals( request.version, metadata.getVersion() );
        Assert.assertEquals( request.name, metadata.getName() );
        Assert.assertEquals( request.description, metadata.getDescription() );
        Assert.assertEquals( request.pluginSiteURL, metadata.getPluginSite() );
        // Assert.assertEquals( request.applicationId, metadata.getGroupId());
        // Assert.assertEquals( request.applicationEdition, metadata.getGroupId());
        // Assert.assertEquals( request.applicationMinVersion, metadata.getGroupId());
        // Assert.assertEquals( request.applicationMaxVersion, metadata.getGroupId());

        // request.licenses.put( "GPL", "http://www.gnu.org/licenses/gpl.txt" );
        // request.licenses.put( "ASF", "http://www.apache.org/licenses/LICENSE-2.0.txt" );

        Assert.assertEquals( request.classpathDependencies.size(), metadata.getClasspathDependencies().size() );
        for ( Dependency dependency : request.classpathDependencies )
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

        Set<String> components = new HashSet<String>( metadata.getComponents() );

        Set<String> expectedComponents = new HashSet<String>();
        expectedComponents.add( "org.sonatype.plugin.test.ComponentExtentionPoint" );
        expectedComponents.add( "org.sonatype.plugin.test.ComponentManaged" );
        expectedComponents.add( "org.sonatype.plugin.test.ManagedViaInterface" );

        // now compare the lists
        Assert.assertEquals( expectedComponents, components );

    }
}
