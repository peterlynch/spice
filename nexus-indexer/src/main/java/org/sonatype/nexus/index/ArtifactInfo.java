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

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * ArtifactInfo holds the values known about an repository artifact. This is a simple Value Object kind of stuff.
 * 
 * @author Jason van Zyl
 */
public class ArtifactInfo
    implements Serializable
{
    private static final long serialVersionUID = 6028843453477511104L;

    // These are attributes that are constant across the versions of a particular artifact.

    public static final String ROOT_GROUPS = "rootGroups";

    public static final String ROOT_GROUPS_VALUE = "rootGroups";

    public static final String ROOT_GROUPS_LIST = "rootGroupsList";
    
    public static final String ALL_GROUPS = "allGroups";

    public static final String ALL_GROUPS_VALUE = "allGroups";

    public static final String ALL_GROUPS_LIST = "allGroupsList";
    

    /**
     * packaging lastModified size sourcesExists javadocExists signatureExists
     */
    public static final String INFO = "i";

    public static final String GROUP_ID = "g";

    public static final String ARTIFACT_ID = "a";

    public static final String PACKAGING = "p";

    public static final String NAME = "n";

    public static final String DESCRIPTION = "d";

    public static final String REPOSITORY = "r";

    // These are attributes that are unique across the versions of a particular artifact.

    /**
     * groupId artifactId version classifier
     */
    public static final String UINFO = "u";

    public static final String ID = "id"; // groupId + artifactId

    public static final String FNAME = "f"; // the artifact filename

    public static final String VERSION = "v";

    public static final String LAST_MODIFIED = "m";

    public static final String SIZE = "s";

    public static final String MD5 = "5";

    public static final String SHA1 = "1";

    public static final String SOURCES_EXISTS = "se";

    public static final String JAVADOC_EXISTS = "je";

    public static final String SIGNATURE_EXISTS = "ae";

    public static final String NAMES = "c";

    public static final String PLUGIN_PREFIX = "px";

    public static final String PLUGIN_GOALS = "gx";

    public static final VersionComparator VERSION_COMPARATOR = new VersionComparator();
    
    
    public String fname;

    public String groupId;

    public String artifactId;

    public String version;

    public String classifier;

    public String packaging;

    public String name;

    public String description;

    public long lastModified = -1;

    public long size = -1;

    public String md5;

    public String sha1;

    public ArtifactAvailablility sourcesExists = ArtifactAvailablility.NOT_PRESENT;

    public ArtifactAvailablility javadocExists = ArtifactAvailablility.NOT_PRESENT;

    public ArtifactAvailablility signatureExists = ArtifactAvailablility.NOT_PRESENT;

    public String classNames;

    public String repository;
    
    public String path;
    
    public String remoteUrl;
    
    public String context;

    /**
     * Plugin goal prefix (only if packaging is "maven-plugin")
     */
    public String prefix;

    /**
     * Plugin goals (only if packaging is "maven-plugin")
     */
    public List<String> goals;


    public ArtifactInfo()
    {
    }

    public ArtifactInfo( String fname, String groupId, String artifactId, String version, String classifier,
        String packaging, String name, String description, long lastModified, long size, String md5, String sha1,
        ArtifactAvailablility sourcesExists, ArtifactAvailablility javadocExists, ArtifactAvailablility signatureExists, String repository )
    {
        // artifact unique
        this.fname = fname;
        this.version = version;
        this.classifier = classifier;
        this.lastModified = lastModified;
        this.size = size;
        this.md5 = md5;
        this.sha1 = sha1;
        this.sourcesExists = sourcesExists;
        this.javadocExists = javadocExists;
        this.signatureExists = signatureExists;
        // artifact constant
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.packaging = packaging;
        this.repository = repository;
        this.name = name;
        this.description = description;
    }

    public ArtifactInfo( String repository, String groupId, String artifactId, String version, String classifier )
    {
        this.repository = repository;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( groupId ).append( ":" ).append( artifactId ).append( ":" ).append( version ).append( ":" ).append(
                classifier ).toString();
    }

    // ----------------------------------------------------------------------------
    // Utils
    // ----------------------------------------------------------------------------

    public static class VersionComparator
        implements Comparator<ArtifactInfo>
    {
        public int compare( ArtifactInfo f1, ArtifactInfo f2 )
        {
            int n = f1.groupId.compareTo( f2.groupId );
            if ( n != 0 )
            {
                return n;
            }

            n = f1.artifactId.compareTo( f2.artifactId );
            if ( n != 0 )
            {
                return n;
            }

            n = -f1.version.compareTo( f2.version );
            if ( n != 0 )
            {
                return n;
            }

            String c1 = f1.classifier;
            String c2 = f2.classifier;
            if ( c1 == null )
            {
                return c2 == null ? 0 : -1;
            }
            else
            {
                return c2 == null ? 1 : c1.compareTo( c2 );
            }
        }
    }

}
