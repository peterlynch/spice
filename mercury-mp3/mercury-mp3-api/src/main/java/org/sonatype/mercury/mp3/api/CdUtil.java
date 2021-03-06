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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.version.DefaultArtifactVersion;
import org.apache.maven.mercury.artifact.version.VersionQuery;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.mercury.mp3.api.cd.AvailableVersions;
import org.sonatype.mercury.mp3.api.cd.ConfigProperty;
import org.sonatype.mercury.mp3.api.cd.ContainerConfig;
import org.sonatype.mercury.mp3.api.cd.DependencyConfig;
import org.sonatype.mercury.mp3.api.cd.LockDownList;
import org.sonatype.mercury.mp3.api.cd.NodeConfig;
import org.sonatype.mercury.mp3.api.cd.Scope;
import org.sonatype.mercury.mp3.api.cd.Version;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.ConfigurationDescriptorXpp3Reader;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.ConfigurationDescriptorXpp3Writer;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.LockDownListXpp3Reader;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.LockDownListXpp3Writer;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.VersionListXpp3Reader;

/**
 * various cd manipulation routines
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class CdUtil
{
    private static final Language LANG = new DefaultLanguage( CdUtil.class );
    
    public static String DEFAULT_SCOPE_NAME = "default";

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

    public static List<ArtifactMetadata> toDepList( List<DependencyConfig> deps )
    {
        if ( deps == null )
            return null;

        if ( deps.isEmpty() )
            return new ArrayList<ArtifactMetadata>( 1 );

        List<ArtifactMetadata> res = new ArrayList<ArtifactMetadata>( deps.size() );

        for ( DependencyConfig dc : deps )
            res.add( new ArtifactMetadata( dc.getName() ) );

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

        if ( Util.isEmpty( nc.getContainers() ) )
            throw new DeltaManagerException( LANG.getMessage( "nc.containers.is.empty" ) );

        List<ContainerConfig> containers = nc.getContainers();

        for ( ContainerConfig cc : containers )
            if ( type.equals( cc.getType() ) && (cid == null || cid.equals( cc.getId() ))  )
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
    public static List<? extends ArtifactMetadata> minus( List<? extends ArtifactMetadata> minuend,
                                                               List<? extends ArtifactMetadata> subtrahend )
    {
        if ( Util.isEmpty( minuend ) )
            return subtrahend;

        if ( Util.isEmpty( subtrahend ) )
            return minuend;

        List<ArtifactMetadata> difference = new ArrayList<ArtifactMetadata>();

        for ( ArtifactMetadata bmd : minuend )
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
    public static void write( List<? extends ArtifactMetadata> deps, File ldlFile )
    throws IOException
    {
        LockDownList ldl = new LockDownList();
        
        if( !Util.isEmpty( deps ) )
            for( ArtifactMetadata bmd : deps )
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
        
        List< ArtifactMetadata> bmdList = toDepList( dcList );
        
        if( Util.isEmpty( bmdList ))
            return null;
        
        List< ArtifactMetadata> mdList = new ArrayList<ArtifactMetadata>( dcList.size() );
        
        for( ArtifactMetadata bmd : bmdList)
            mdList.add( new ArtifactMetadata(bmd) );
        
        return mdList;
    }
    //----------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static List<ArtifactMetadata> getVersions( File versFile, String scope )
    throws IOException, XmlPullParserException
    {
        VersionListXpp3Reader reader = new VersionListXpp3Reader();
        InputStream is = new FileInputStream(versFile);
        AvailableVersions versions = reader.read( is );
        is.close();
        
        if( versions ==null )
            throw new IOException( LANG.getMessage( "no.versions" ) );
        
        if( Util.isEmpty( versions.getScopes() ) )
            throw new IOException( LANG.getMessage( "no.scopes" ) );
        
        List<Scope> sl = versions.getScopes(); 
        
        for( Scope s : sl )
            if( scope.equals( s.getName() ) )
            {
                List<Version> vl = s.getVersions();
                
                if( Util.isEmpty( vl ) )
                    throw new IOException( LANG.getMessage( "no.scoped.versions", scope) );
                
                List<ArtifactMetadata> res = new ArrayList<ArtifactMetadata>( vl.size() );
                
                for( Version v : vl )
                {
                    ArtifactMetadata bmd = new ArtifactMetadata( v.getName() ); 
                    bmd.setType( DeltaManager.CD_EXT );
                    res.add( bmd );
                }
                
                Collections.sort( res, new Comparator<ArtifactMetadata>()
                      {
                         public int compare( ArtifactMetadata o1, ArtifactMetadata o2 )
                         {
                             return new DefaultArtifactVersion(o2.getVersion()).compareTo( new DefaultArtifactVersion(o1.getVersion()) );
                         }
                      }
                                );
                return res;
            }
        
        return null;
    }
    //----------------------------------------------------------------------------------------------------
    public static final List<? extends ArtifactMetadata> binDiff( List<? extends ArtifactMetadata> in, List<Artifact> out )
    {
        if( Util.isEmpty( in ) )
            return null;
        
        if( Util.isEmpty( out ) )
            return in;
        
        List<ArtifactMetadata> res = new ArrayList<ArtifactMetadata>( in.size() );
        
        for( ArtifactMetadata bmd : in )
        {
            boolean notExists = true;
            String gid = bmd.getGroupId();
            String aid = bmd.getArtifactId();
            String ver = bmd.getVersion();
            String cls = bmd.getClassifier() == null ? "---" : bmd.getClassifier();
            String typ = bmd.getType() == null ? "jar" : bmd.getType();
            
            for( Artifact a : out )
                if( a.getGroupId().equals( gid )
                    && a.getArtifactId().equals( aid )
                    && a.getVersion().equals( ver )
                    && a.getVersion().equals( ver )
                 )
                {
                    String acls = a.getClassifier() == null ? "---" : a.getClassifier();
                    String atyp = a.getType() == null ? "jar" : a.getType();
                    
                    if( acls.equals( cls ) && atyp.equals( typ ) )
                    {
                        notExists = false;
                        break;
                    }
                }

            if( notExists )
                res.add( bmd );
        }
        
        return res;
            
    }
    //----------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------
}
