/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.index;

import java.io.File;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.M2GavCalculator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.locator.ArtifactLocator;
import org.sonatype.nexus.index.locator.Locator;
import org.sonatype.nexus.index.locator.MetadataLocator;
import org.sonatype.nexus.index.locator.PomLocator;

/**
 * The default implementation of the ArtifactContextProducer.
 * 
 * @author cstamas
 * @author Eugene Kuleshov
 * 
 * @plexus.component
 */
public class DefaultArtifactContextProducer
    implements ArtifactContextProducer
{
    private Locator al = new ArtifactLocator();

    private Locator pl = new PomLocator();

    private Locator ml = new MetadataLocator();

    /**
     * Get ArtifactContext for given pom or artifact (jar, war, etc). A file can be
     */
    public ArtifactContext getArtifactContext( IndexingContext context, File file )
    {
        // TODO shouldn't this use repository layout instead?
        Gav gav = M2GavCalculator.calculate( file.getAbsolutePath().substring(
            context.getRepository().getAbsolutePath().length() + 1 ).replace( '\\', '/' ) );

        if ( gav == null )
        {
            // XXX what then? Without GAV we are screwed (look below).
            // It should simply stop, since it is not an artifact.
            return null;
        }

        String groupId = gav.getGroupId();
        
        String artifactId = gav.getArtifactId();
        
        String version = gav.getVersion();
        
        String classifier = gav.getClassifier();
        
        File pom;
        
        File artifact;

        if ( file.getName().endsWith( ".pom" ) )
        {
            pom = file;

            // XXX this need to be fixed to handle non jar artifacts
            artifact = al.locate( file, gav );
        }
        else
        {
            artifact = file;

            pom = pl.locate( file, gav );

            if ( !pom.exists() )
            {
                return null;
            }
        }

        ArtifactInfo ai = new ArtifactInfo(
            context.getRepositoryId(), 
            groupId,
            artifactId, 
            version, 
            classifier);
            
//        ArtifactInfo ai = new ArtifactInfo(
//            fname,
//            groupId,
//            artifactId,
//            version,
//            classifier,
//            packaging,
//            name,
//            description,
//            artifact.lastModified(),
//            artifact.length(),
//            md5Text,
//            sha1Text,
//            sourcesExists,
//            javadocExists,
//            signatureExists,
//            context.getRepositoryId() );
        
        File metadata = ml.locate( pom, gav );
        
        return new ArtifactContext( pom, artifact, metadata, ai );
    }

}
