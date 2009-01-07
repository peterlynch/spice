/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package net.java.dev.openim.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.logging.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XStreamStore
{
    public static final String DEFAULT_ENCODING = "UTF-8";

    private File file;
    private XStream xstream;
    private Logger logger;
    private Map map;
    
    private String substituteFrom;
    private String substituteTo;
    private String xmlProlog;

    public XStreamStore( File file, Logger logger )
    {
        this( file, logger, DEFAULT_ENCODING );
    }

    public XStreamStore( File file, Logger logger, String encoding )
    {
        this.file = file;
        this.logger = logger;
        xstream = new XStream( new DomDriver() );
        String enc = encoding != null ? encoding : DEFAULT_ENCODING;
        xmlProlog = "<?xml version='1.0' encoding='" + enc + "'?>";
    }

    public void load()
    {
        map = loadMap();
    }

    //  --------------------------------------------------------------------------    
    public void alias( String name, Class classz )
    {
        xstream.alias( name, classz );
    }

    //  --------------------------------------------------------------------------    
    public void substitute( String from, String to )
    {
        substituteFrom = from;
        substituteTo = to;
    }

    //  --------------------------------------------------------------------------
    public Object get( Object key )
    {
        Object value = null;
        synchronized ( map )
        {
            value = map.get( key );
        }
        return value;
    }

    //  --------------------------------------------------------------------------
    public Object remove( Object key )
    {
        Object value = null;
        synchronized ( map )
        {
            value = map.remove( key );
            saveMap( map );
        }
        return value;
    }

    //  --------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void put( Object key, Object value )
    {
        synchronized ( map )
        {
            value = map.put( key, value );
            saveMap( map );
        }
    }

    //  --------------------------------------------------------------------------    
    private Logger getLogger()
    {
        return logger;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    private void saveMap( Map map )
    {
        String xstreamData = xstream.toXML( map );
        if ( substituteFrom != null && substituteTo != null )
        {
            xstreamData = StringUtils.replace( xstreamData, substituteFrom, substituteTo );
        }
        xstreamData = xmlProlog + "\n" + xstreamData;
        //getLogger().info("saving roster " + xstreamData);
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream( file );
            fos.write( xstreamData.getBytes() );
        }
        catch ( IOException e )
        {
            getLogger().error( e.getMessage(), e );
        }
        finally
        {
            if ( fos != null )
            {
                try
                {
                    fos.close();
                }
                catch ( IOException e )
                {
                    getLogger().error( e.getMessage() );
                }
            }
        }

    }

    //--------------------------------------------------------------------------
    private Map loadMap()
    {
        Map map = null;

        if ( file.exists() )
        {
            try
            {
                FileInputStream fis = new FileInputStream( file );
                String xmlData = IOUtils.toString( fis );
                fis.close();
                if ( substituteFrom != null && substituteTo != null )
                {
                    xmlData = StringUtils.replace( xmlData, substituteTo, substituteFrom );
                }
                map = (Map) xstream.fromXML( xmlData );
            }
            catch ( Exception e )
            {
                getLogger().error( e.getMessage(), e );
            }

        }
        else
        {
            getLogger().info( "No " + file + " => starting with void store" );
            map = new HashMap();
        }

        return map;
    }

}
