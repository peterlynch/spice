/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.plexus.component.bundlepublisher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.plexus.component.bundlepublisher.model.ArtifactDependency;
import org.sonatype.plexus.component.bundlepublisher.model.BundleArtifact;
import org.sonatype.plexus.component.bundlepublisher.model.BundleDescriptor;

/**
 * @plexus.component
 */
public class DefaultBundlePublisher
    extends AbstractLogEnabled
    implements BundlePublisher, Contextualizable
{

    private static final File TEMP_DIR = new File( System.getProperty( "java.io.tmpdir" ) );

    private static final Random RANDOM = new Random();

    /**
     * @plexus.requirement
     */
    private ArtifactFactory artifactFactory;

    /**
     * @plexus.requirement
     */
    private ArtifactDeployer deployer;

    /**
     * @plexus.requirement
     */
    private ArtifactInstaller installer;

    /**
     * @plexus.requirement role="org.codehaus.plexus.archiver.UnArchiver" role-hint="zip"
     */
    private ZipUnArchiver zipUnArchiver;

    private final List<File> temporaryFiles = new ArrayList<File>();

    private PlexusContainer plexus;

    public void deploy( File sourceFile, InputStream bundleDescriptor, ArtifactRepository deploymentRepository,
                        ArtifactRepository localRepository )
        throws PublishingException
    {
        Collection<Artifact> artifacts = preparePublish( sourceFile, bundleDescriptor );

        try
        {
            for ( Artifact artifact : artifacts )
            {
                deployer.deploy( artifact.getFile(), artifact, deploymentRepository, localRepository );
            }
        }
        catch ( ArtifactDeploymentException e )
        {
            throw new PublishingException( "Unable to deploy artifact: " + e.getMessage(), e );
        }
        finally
        {
            killTemporaryFiles();
        }
    }

    public void install( File sourceFile, InputStream bundleDescriptor, ArtifactRepository localRepository )
        throws PublishingException
    {
        Collection<Artifact> artifacts = preparePublish( sourceFile, bundleDescriptor );

        try
        {
            for ( Artifact artifact : artifacts )
            {
                installer.install( artifact.getFile(), artifact, localRepository ); // to install
            }
        }
        catch ( ArtifactInstallationException e )
        {
            throw new PublishingException( "Unable to install artifact: " + e.getMessage(), e );
        }
        finally
        {
            killTemporaryFiles();
        }

    }

    private Collection<Artifact> preparePublish( File sourceFile, InputStream bundleDescriptor )
        throws PublishingException
    {
        validate( sourceFile, bundleDescriptor );

        BundleDescriptor descriptor;
        try
        {
            descriptor = BundleDescriptor.read( bundleDescriptor );
        }
        catch ( Exception e )
        {
            throw new PublishingException( "Unable to parse descriptor file", e );
        }

        Collection<Artifact> artifacts = new ArrayList<Artifact>();

        ZipFile zip = null;
        try
        {
            zip = new ZipFile( sourceFile );
            validate( descriptor, zip );

            File bundleDir = createTempFile( sourceFile.getName(), "bundle" );
            bundleDir.mkdirs();

            zipUnArchiver.setSourceFile( sourceFile );
            zipUnArchiver.setDestDirectory( bundleDir );
            zipUnArchiver.extract();

            for ( BundleArtifact artifact : descriptor.getArtifacts() )
            {
                getLogger().debug( "Importing artifact " + artifact.getArtifactId() );

                Artifact mavenArtifact = createMavenArtifact( descriptor, artifact );

                File file;
                if ( "pom".equals( mavenArtifact.getType() ) )
                {
                    Model pom = createMavenModel( descriptor, artifact );
                    file = toFile( pom );
                }
                else
                {
                    String location = artifact.getLocation();
                    file = getArtifactFile( bundleDir, location, mavenArtifact );

                    if ( artifact.getClassifier() == null )
                    {
                        Model pom = createMavenModel( descriptor, artifact );
                        Artifact pomArtifact = createMavenArtifact( descriptor, artifact, "pom" );
                        pomArtifact.setFile( toFile( pom ) );
                        artifacts.add( pomArtifact );
                    }
                }

                mavenArtifact.setFile( file );

                artifacts.add( mavenArtifact );
            }

            String groupId = descriptor.getDefaults().getGroupId();
            String artifactId = "bundle";
            String version = descriptor.getDefaults().getVersion();
            String type = "zip";

            Artifact mavenArtifact =
                artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, type, null );
            mavenArtifact.setFile( sourceFile );
            artifacts.add( mavenArtifact );
        }
        catch ( IOException e )
        {
            throw new PublishingException( e.getMessage(), e );
        }
        catch ( ArchiverException e )
        {
            throw new PublishingException( e.getMessage(), e );
        }
        finally
        {
            if ( zip != null )
            {
                try
                {
                    zip.close();
                }
                catch ( IOException e )
                {
                    // just closing
                }
            }
        }

        return artifacts;
    }

    private File toFile( Model pom )
        throws IOException
    {
        FileWriter writer = null;
        try
        {
            File file = createTempFile( pom.getArtifactId(), pom.getPackaging() );
            file.createNewFile();

            writer = new FileWriter( file );
            new MavenXpp3Writer().write( writer, pom );

            return file;
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    private File getArtifactFile( File bundleDir, String location, Artifact mavenArtifact )
        throws IOException, ArchiverException, PublishingException
    {

        File artifactFile = new File( bundleDir, location );
        if ( artifactFile.isDirectory() )
        {
            File zipFile = createTempFile( mavenArtifact.getArtifactId(), mavenArtifact.getType() );
            zipFile.createNewFile();

            ZipArchiver zipArchiver;
            try
            {
                zipArchiver = (ZipArchiver) plexus.lookup( Archiver.ROLE, "zip" );
            }
            catch ( ComponentLookupException e )
            {
                throw new PublishingException( "Unable to lookup for ZipArchiver", e );
            }
            zipArchiver.reset();
            for ( File file : artifactFile.listFiles() )
            {
                if ( file.isFile() )
                {
                    zipArchiver.addFile( file, file.getName() );
                }
            }
            zipArchiver.setDestFile( zipFile );
            zipArchiver.createArchive();

            return zipFile;
        }
        else
        {
            return artifactFile;
        }
    }

    private void killTemporaryFiles()
    {
        for ( File tempFile : temporaryFiles )
        {
            try
            {
                FileUtils.forceDelete( tempFile );
            }
            catch ( IOException e )
            {
                // just cleaning
                getLogger().warn( "Unable to delete temporary file " + tempFile.getAbsolutePath(), e );
            }
        }
    }

    private Artifact createMavenArtifact( BundleDescriptor descriptor, BundleArtifact artifact )
    {
        String type = artifact.getType() != null ? artifact.getType() : "jar";
        return createMavenArtifact( descriptor, artifact, type );
    }

    private Artifact createMavenArtifact( BundleDescriptor descriptor, BundleArtifact artifact, String type )
    {
        String groupId = artifact.getGroupId() != null ? artifact.getGroupId() : descriptor.getDefaults().getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion() != null ? artifact.getVersion() : descriptor.getDefaults().getVersion();
        String classifier = artifact.getClassifier();

        Artifact mavenArtifact =
            artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, type, classifier );

        return mavenArtifact;
    }

    private Model createMavenModel( BundleDescriptor descriptor, BundleArtifact artifact )
        throws PublishingException
    {
        String groupId = artifact.getGroupId() != null ? artifact.getGroupId() : descriptor.getDefaults().getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion() != null ? artifact.getVersion() : descriptor.getDefaults().getVersion();
        String type = artifact.getType() != null ? artifact.getType() : "jar";

        Model pom = new Model();
        pom.setModelVersion( "4.0.0" );
        pom.setGroupId( groupId );
        pom.setArtifactId( artifactId );
        pom.setVersion( version );
        pom.setPackaging( type );
        if ( descriptor.getOrganization() != null )
        {
            Organization organization = new Organization();
            organization.setName( descriptor.getOrganization().getName() );
            organization.setUrl( descriptor.getOrganization().getUrl() );
            pom.setOrganization( organization );

            String licenseText = descriptor.getOrganization().getLicense();
            if ( licenseText != null )
            {
                License license = new License();
                license.setComments( licenseText );
                pom.addLicense( license );
            }
        }

        for ( ArtifactDependency dependency : artifact.getDependencies() )
        {
            String depGroupId =
                dependency.getGroupId() != null ? dependency.getGroupId() : descriptor.getDefaults().getGroupId();
            String depVersion = dependency.getVersion() != null ? dependency.getVersion() : version;
            String depType = dependency.getType() != null ? dependency.getType() : "jar";

            Dependency dep = new Dependency();
            dep.setGroupId( depGroupId );
            dep.setArtifactId( dependency.getArtifactId() );
            dep.setClassifier( dependency.getClassifier() );
            dep.setType( depType );
            dep.setVersion( depVersion );
            pom.addDependency( dep );
        }

        return pom;
    }

    private void validate( BundleDescriptor descriptor, ZipFile zip )
        throws PublishingException
    {
        getLogger().debug( "Validating descriptor" );

        if ( descriptor.getDefaults() == null )
        {
            throw new PublishingException( "Invalid descriptor: Defaults is not defined!" );
        }
        if ( descriptor.getDefaults().getGroupId() == null )
        {
            throw new PublishingException( "Invalid descriptor: Default groupId is not defined!" );
        }
        if ( descriptor.getDefaults().getVersion() == null )
        {
            throw new PublishingException( "Invalid descriptor: Default version is not defined!" );
        }

        if ( descriptor.getArtifacts().isEmpty() )
        {
            throw new PublishingException( "Invalid descriptor: No artifacts defined!" );
        }

        for ( BundleArtifact artifact : descriptor.getArtifacts() )
        {
            String artifactId = artifact.getArtifactId();
            if ( artifactId == null )
            {
                throw new PublishingException( "Invalid descriptor: Artifact ID not defined!" );
            }
            if ( artifact.getLocation() == null )
            {
                if ( !"pom".equals( artifact.getType() ) )
                {
                    throw new PublishingException( "Invalid descriptor: Artifact location not defined for: "
                        + artifactId );
                }

            }
            else
            {
                ZipEntry entry = zip.getEntry( artifact.getLocation() );
                if ( entry == null )
                {
                    throw new PublishingException( "Artifact for '" + artifactId + "' not found on sourceFile: "
                        + artifact.getLocation() );
                }

                if ( entry.isDirectory() )
                {
                    if ( !"zip".equals( artifact.getType() ) )
                    {
                        throw new PublishingException( "Invalid location for '" + artifactId
                            + "' directory are only allowed to zip packaging artifacts." );
                    }
                }
            }

            List<ArtifactDependency> dependencies = artifact.getDependencies();
            for ( ArtifactDependency dependency : dependencies )
            {
                if ( dependency.getArtifactId() == null )
                {
                    throw new PublishingException( "Invalid descriptor: Dependency artifactId not defined! Artifact "
                        + artifactId );
                }
            }

        }
    }

    public void validate( File sourceFile, InputStream bundleDescriptor )
        throws PublishingException
    {
        getLogger().debug( "Validating inputs" );
        if ( sourceFile == null )
        {
            throw new PublishingException( "Source file not defined. Please define a valid source file!" );
        }

        if ( !sourceFile.exists() )
        {
            throw new PublishingException( "Unable to find source file: " + sourceFile.getAbsolutePath() );
        }

        if ( bundleDescriptor == null )
        {
            throw new PublishingException( "Bundle descriptor not defined. Please define a valid bundle descriptor!" );
        }
    }

    private File createTempFile( String prefix, String suffix )
    {
        File tempFile = new File( TEMP_DIR, prefix + "-" + Long.toHexString( RANDOM.nextInt() ) + "-" + suffix );
        tempFile.getParentFile().mkdirs();

        temporaryFiles.add( tempFile );
        return tempFile;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        plexus = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

}
