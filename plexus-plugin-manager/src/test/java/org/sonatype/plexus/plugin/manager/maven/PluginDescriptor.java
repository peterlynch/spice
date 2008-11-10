package org.sonatype.plexus.plugin.manager.maven;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jason van Zyl
 * @version $Id$
 */
public class PluginDescriptor
    extends ComponentSetDescriptor
{
    private String groupId;

    private String artifactId;

    private String version;

    private String goalPrefix;

    private String source;

    private boolean inheritedByDefault = true;

    private List artifacts;

    private Map lifecycleMappings;

    private ClassRealm classRealm;

    // calculated on-demand.
    private Map artifactMap;

    private Set introducedDependencyArtifacts;

    private String name;

    private String description;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List getMojos()
    {
        return getComponents();
    }

    public void addMojo( MojoDescriptor mojoDescriptor )
        throws DuplicateMojoDescriptorException
    {
        MojoDescriptor existing = null;
        // this relies heavily on the equals() and hashCode() for ComponentDescriptor,
        // which uses role:roleHint for identity...and roleHint == goalPrefix:goal.
        // role does not vary for Mojos.
        List mojos = getComponents();

        if ( mojos != null && mojos.contains( mojoDescriptor ) )
        {
            int indexOf = mojos.indexOf( mojoDescriptor );

            existing = (MojoDescriptor) mojos.get( indexOf );
        }

        if ( existing != null )
        {
            throw new DuplicateMojoDescriptorException( getGoalPrefix(), mojoDescriptor.getGoal(), existing
                .getImplementation(), mojoDescriptor.getImplementation() );
        }
        else
        {
            addComponentDescriptor( mojoDescriptor );
        }
    }

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

    // ----------------------------------------------------------------------
    // Dependencies
    // ----------------------------------------------------------------------

    public static String constructPluginKey( String groupId, String artifactId, String version )
    {
        return groupId + ":" + artifactId + ":" + version;
    }

    public String getPluginLookupKey()
    {
        return groupId + ":" + artifactId;
    }

    public String getId()
    {
        return constructPluginKey( groupId, artifactId, version );
    }

    public static String getDefaultPluginArtifactId( String id )
    {
        return "maven-" + id + "-plugin";
    }

    public static String getDefaultPluginGroupId()
    {
        return "org.apache.maven.plugins";
    }

    /**
     * Parse maven-...-plugin.
     *
     * @todo move to plugin-tools-api as a default only
     */
    public static String getGoalPrefixFromArtifactId( String artifactId )
    {
        if ( "maven-plugin-plugin".equals( artifactId ) )
        {
            return "plugin";
        }
        else
        {
            return artifactId.replaceAll( "-?maven-?", "" ).replaceAll( "-?plugin-?", "" );
        }
    }

    public String getGoalPrefix()
    {
        return goalPrefix;
    }

    public void setGoalPrefix( String goalPrefix )
    {
        this.goalPrefix = goalPrefix;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getVersion()
    {
        return version;
    }

    public void setSource( String source )
    {
        this.source = source;
    }

    public String getSource()
    {
        return source;
    }

    public boolean isInheritedByDefault()
    {
        return inheritedByDefault;
    }

    public void setInheritedByDefault( boolean inheritedByDefault )
    {
        this.inheritedByDefault = inheritedByDefault;
    }

    public List getArtifacts()
    {
        return artifacts;
    }

    public void setArtifacts( List artifacts )
    {
        this.artifacts = artifacts;

        // clear the calculated artifactMap
        artifactMap = null;
    }

    public boolean equals( Object object )
    {
        if ( this == object )
        {
            return true;
        }

        return getId().equals( ( (PluginDescriptor) object ).getId() );
    }

    public int hashCode()
    {
        return 10 + getId().hashCode();
    }

    public MojoDescriptor getMojo( String goal )
    {
        if ( getMojos() == null )
        {
            return null; // no mojo in this POM
        }

        // TODO: could we use a map? Maybe if the parent did that for components too, as this is too vulnerable to
        // changes above not being propogated to the map

        MojoDescriptor mojoDescriptor = null;
        for ( Iterator i = getMojos().iterator(); i.hasNext() && mojoDescriptor == null; )
        {
            MojoDescriptor desc = (MojoDescriptor) i.next();
            if ( goal.equals( desc.getGoal() ) )
            {
                mojoDescriptor = desc;
            }
        }
        return mojoDescriptor;
    }

    public void setClassRealm( ClassRealm classRealm )
    {
        this.classRealm = classRealm;
    }

    public ClassRealm getClassRealm()
    {
        return classRealm;
    }

    public void setIntroducedDependencyArtifacts( Set introducedDependencyArtifacts )
    {
        this.introducedDependencyArtifacts = introducedDependencyArtifacts;
    }

    public Set getIntroducedDependencyArtifacts()
    {
        return introducedDependencyArtifacts != null ? introducedDependencyArtifacts : Collections.EMPTY_SET;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }
}
