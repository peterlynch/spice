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
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.mercury.mp3.api.cd.ConfigProperty;
import org.sonatype.mercury.mp3.api.cd.DependencyConfig;
import org.sonatype.mercury.mp3.api.cd.NodeConfig;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.ConfigurationDescriptorXpp3Reader;
import org.sonatype.mercury.mp3.api.cd.io.xpp3.ConfigurationDescriptorXpp3Writer;

/**
 * various cd manipulation routines
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class CdUtil
{
    public static Map<String,String> propsToMap( List<ConfigProperty> props )
    {
        if( props == null )
            return null;
        
        if( props.isEmpty() )
            return new HashMap<String, String>(1);
        
        Map<String, String> res = new HashMap<String, String>( props.size() );
        
        for( ConfigProperty p : props )
        {
            res.put( p.getName(), p.getValue() );
        }
        
        return res;
    }
    
    public static List<ArtifactBasicMetadata> toDepList( List<DependencyConfig> deps )
    {
        if( deps == null )
            return null;
        
        if( deps.isEmpty() )
            return new ArrayList<ArtifactBasicMetadata>(1);
        
        List<ArtifactBasicMetadata> res = new ArrayList<ArtifactBasicMetadata>( deps.size() );
        
        for( DependencyConfig dc : deps )
        {
            res.add( new ArtifactBasicMetadata( dc.getName() ) );
        }
        
        return res;
    }
    
    public static NodeConfig read( File cf )
    throws FileNotFoundException, IOException, XmlPullParserException
    {
        ConfigurationDescriptorXpp3Reader reader = new ConfigurationDescriptorXpp3Reader();
        
        NodeConfig res = reader.read( new FileInputStream(cf) );
        
        return res;
    }
    
    public static void write( NodeConfig nc, File cf )
    throws FileNotFoundException, IOException
    {
        ConfigurationDescriptorXpp3Writer writer = new ConfigurationDescriptorXpp3Writer();
        
        writer.write( new FileWriter(cf), nc );
    }
}
