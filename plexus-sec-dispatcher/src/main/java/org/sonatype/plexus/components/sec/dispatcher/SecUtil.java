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
 
package org.sonatype.plexus.components.sec.dispatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sonatype.plexus.components.sec.dispatcher.model.Config;
import org.sonatype.plexus.components.sec.dispatcher.model.ConfigProperty;
import org.sonatype.plexus.components.sec.dispatcher.model.Sec;
import org.sonatype.plexus.components.sec.dispatcher.model.io.xpp3.SecurityConfigurationXpp3Reader;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class SecUtil
{
    public static final String [] URL_PROTOCOLS = new String [] {"http://","https://","file://","dav://","davs://","webdav://","webdavs://","dav+http://","dav+https://"};

    public static Sec read( String location, boolean cycle )
    throws SecDispatcherException
    {
        if( location == null )
            throw new SecDispatcherException("location to read from is null");
        
        InputStream in;
        try
        {
            in = toStream( location );
        
            Sec sec = new SecurityConfigurationXpp3Reader().read( in );
            
            in.close();
            
            if( cycle && sec.getRelocation() != null )
                return read( sec.getRelocation(), true );
            
            return sec;
        }
        catch ( Exception e )
        {
            throw new SecDispatcherException(e);
        }
    }
    //---------------------------------------------------------------------------------------------------------------
    public static InputStream toStream( String resource )
    throws MalformedURLException, IOException
    {
      if( resource == null )
        return null;
      
      String lowerRes = resource.toLowerCase();
      
      for( int i=0; i<URL_PROTOCOLS.length; i++ )
      {
          String p = URL_PROTOCOLS[i];
          
          if( lowerRes.startsWith( p ) )
            return new URL(resource).openStream();
      }

      return new FileInputStream( new File(resource) );
    }
    //---------------------------------------------------------------------------------------------------------------
    public static Map getConfig( Sec sec, String name )
    {
        if( name == null )
            return null;
        
        List cl = sec.getConfigurations();
        
        if( cl == null )
            return null;
        
        for( Iterator i = cl.iterator(); i.hasNext(); )
        {
            Config cf = (Config) i.next();
            
            if( !name.equals( cf.getName() ) )
                continue;
            
            List pl = cf.getProperties();
            
            if( pl == null || pl.isEmpty() )
                return null;
            
            Map res = new HashMap( pl.size() );

            for( Iterator j = pl.iterator(); j.hasNext(); )
            {
                ConfigProperty p = (ConfigProperty) j.next();
                
                res.put( p.getName(), p.getValue() );
            }
            
            return res;
        }
        
        return null;
    }
    //---------------------------------------------------------------------------------------------------------------
}
