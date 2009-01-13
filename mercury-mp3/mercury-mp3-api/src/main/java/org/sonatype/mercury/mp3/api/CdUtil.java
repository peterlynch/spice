/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package org.sonatype.mercury.mp3.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.mercury.mp3.api.cd.ConfigProperty;
import org.sonatype.mercury.mp3.api.cd.ContainerConfig;
import org.sonatype.mercury.mp3.api.cd.DependencyConfig;
import org.sonatype.mercury.mp3.api.cd.LockDownList;
import org.sonatype.mercury.mp3.api.cd.NodeConfig;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.ConfigurationDescriptorXpp3Reader;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.ConfigurationDescriptorXpp3Writer;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.LockDownListXpp3Reader;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.LockDownListXpp3Writer;

/**
 * various cd manipulation routines
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class CdUtil
{
    private static final Language LANG = new DefaultLanguage( CdUtil.class );

    public static Map<String, String> propsToMap( List<ConfigProperty> props )
    {
        if ( props == null )
            return null;

        if ( props.isEmpty() )
            return new HashMap<String, String>( 1 );

        Map<String, String> res = new HashMap<String, String>( props.size() );

        for ( ConfigProperty p : props )
        {
            res.put( p.getName(), p.getValue() );
        }

        return res;
    }

    public static List<ArtifactBasicMetadata> toDepList( List<DependencyConfig> deps )
    {
        if ( deps == null )
            return null;

        if ( deps.isEmpty() )
            return new ArrayList<ArtifactBasicMetadata>( 1 );

        List<ArtifactBasicMetadata> res = new ArrayList<ArtifactBasicMetadata>( deps.size() );

        for ( DependencyConfig dc : deps )
            res.add( new ArtifactBasicMetadata( dc.getName() ) );

        return res;
    }

    @SuppressWarnings("unchecked")
    public static ContainerConfig findContainer( NodeConfig nc, String type, String cid )
        throws DeltaManagerException
    {
        if ( nc == null )
            throw new IllegalArgumentException( LANG.getMessage( "config.is.null" ) );

        if ( Util.isEmpty( type ) )
            throw new IllegalArgumentException( LANG.getMessage( "container.type.is.null" ) );

        if ( Util.isEmpty( cid ) )
            throw new IllegalArgumentException( LANG.getMessage( "container.id.is.null" ) );

        if ( Util.isEmpty( nc.getContainers() ) )
            throw new DeltaManagerException( LANG.getMessage( "nc.containers.is.empty" ) );

        List<ContainerConfig> containers = nc.getContainers();

        for ( ContainerConfig cc : containers )
            if ( cid.equals( cc.getId() ) && type.equals( cc.getType() ) )
                return cc;

        throw new DeltaManagerException( LANG.getMessage( "container.not.found", type, cid, nc.getId() ) );
    }

    public static NodeConfig read( File cf )
        throws FileNotFoundException, IOException, XmlPullParserException
    {
        ConfigurationDescriptorXpp3Reader reader = new ConfigurationDescriptorXpp3Reader();

        NodeConfig res = reader.read( new FileInputStream( cf ) );

        return res;
    }

    public static void write( NodeConfig nc, File cf )
        throws FileNotFoundException, IOException
    {
        ConfigurationDescriptorXpp3Writer writer = new ConfigurationDescriptorXpp3Writer();

        writer.write( new FileWriter( cf ), nc );
    }

    /**
     * calculate set diff
     * 
     * @param minuend
     * @param subtrahend
     * @return
     */
    public static List<? extends ArtifactBasicMetadata> minus( List<? extends ArtifactBasicMetadata> minuend,
                                                               List<? extends ArtifactBasicMetadata> subtrahend )
    {
        if ( Util.isEmpty( minuend ) )
            return subtrahend;

        if ( Util.isEmpty( subtrahend ) )
            return minuend;

        List<ArtifactBasicMetadata> difference = new ArrayList<ArtifactBasicMetadata>();

        for ( ArtifactBasicMetadata bmd : minuend )
            if ( !subtrahend.contains( bmd ) )
                difference.add( bmd );

        return difference;
    }
    // ----------------------------------------------------------------------------------------------------
    /**
     * @param deps
     * @param ldlFile
     * @throws IOException 
     */
    public static void write( List<? extends ArtifactBasicMetadata> deps, File ldlFile )
    throws IOException
    {
        LockDownList ldl = new LockDownList();
        
        if( !Util.isEmpty( deps ) )
            for( ArtifactBasicMetadata bmd : deps )
            {
                DependencyConfig dc = new DependencyConfig();
                
                dc.setName( bmd.getGAV() );
                
                ldl.addDependency( dc );
            }
            
        LockDownListXpp3Writer wr = new LockDownListXpp3Writer();
        
        ldlFile.getParentFile().mkdirs();

        FileWriter fw = new FileWriter( ldlFile );
        
        wr.write( fw, ldl );
        
        fw.flush();
        
        fw.close();
    }
    // ----------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static List<ArtifactMetadata> readLdl( File ldlFile )
    throws FileNotFoundException, IOException, XmlPullParserException
    {
        if( ldlFile == null || !ldlFile.exists() )
            throw new FileNotFoundException( ldlFile == null ? "null" : ldlFile.getAbsolutePath() );
        
        LockDownListXpp3Reader rd = new LockDownListXpp3Reader();
        
        LockDownList ldl = rd.read( new FileInputStream(ldlFile) );
        
        List< DependencyConfig > dcList = ldl.getDependencies();
        
        List< ArtifactBasicMetadata> bmdList = toDepList( dcList );
        
        if( Util.isEmpty( bmdList ))
            return null;
        
        List< ArtifactMetadata> mdList = new ArrayList<ArtifactMetadata>( dcList.size() );
        
        for( ArtifactBasicMetadata bmd : bmdList)
            mdList.add( new ArtifactMetadata(bmd) );
        
        return mdList;
    }
    // ----------------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------------
}
