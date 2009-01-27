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


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.plexus.components.sec.dispatcher.model.Sec;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

/**
 * @plexus.component
 * @author Oleg Gusakov</a>
 */
public class DefaultSecDispatcher
extends AbstractLogEnabled
implements SecDispatcher
{
    public static final String SYSTEM_PROPERTY_SEC_LOCATION = "maven.sec.path";
    
    public static final String TYPE_ATTR = "type";

    public static final char ATTR_START = '[';

    public static final char ATTR_STOP  = ']';

    /**
     * DefaultHandler
     * 
     * @plexus.requirement
     */
    protected PlexusCipher _cipher;

    // ---------------------------------------------------------------
    public String decrypt( String str, Map attributes, Map config, PlexusContainer plexus )
        throws SecDispatcherException
    {
        if( ! isEncryptedString( str ) )
            return str;
        
        String bare = null;
        
        try
        {
            bare = _cipher.unDecorate( str );
        }
        catch ( PlexusCipherException e1 )
        {
            throw new SecDispatcherException( e1 );
        }
        
        try
        {
            Map attr = stripAttributes( bare );
            
            String res = null;

            Sec sec = getSec();
            
            if( attr == null || attr.get( "type" ) == null )
            {
                String master = getMaster( sec );
                
                res = _cipher.decrypt( bare, master );
            }
            else
            {
                String type = (String) attr.get( TYPE_ATTR );
                
                if( plexus == null )
                    throw new SecDispatcherException( "plexus container not supplied - cannot lookup "+type );
                
                Map conf = SecUtil.getConfig( sec, type );
                
                SecDispatcher dispatcher = (SecDispatcher) plexus.lookup( SecDispatcher.ROLE, type );
                
                String pass = attr == null ? bare : strip( bare );
                
                return dispatcher.decrypt( pass, attr, conf, plexus );
            }
            
            return res;
        }
        catch ( Exception e )
        {
            throw new SecDispatcherException(e);
        }
    }
    
    private String strip( String str )
    {
        int pos = str.indexOf( ATTR_STOP );
        
        if( pos == str.length() )
            return null;
        
        if( pos != -1 )
            return str.substring( pos+1 );
        
        return str;
    }
    
    private Map stripAttributes( String str )
    {
        int start = str.indexOf( ATTR_START );
        int stop = str.indexOf( ATTR_STOP );
        if ( start != -1 && stop != -1 && stop > start )
        {
            if( stop == start+1 )
                return null;
            
            String attrs = str.substring( start+1, stop ).trim();
            
            if( attrs == null || attrs.length() < 1 )
                return null;
            
            Map res = null;
            
            StringTokenizer st = new StringTokenizer( attrs, ", " );
            
            while( st.hasMoreTokens() )
            {
                if( res == null )
                    res = new HashMap( st.countTokens() );
                
                String pair = st.nextToken();
                
                int pos = pair.indexOf( '=' );
                
                if( pos == -1 )
                    continue;
                
                String key = pair.substring( 0, pos ).trim();

                if( pos == pair.length() )
                {
                    res.put( key, null );
                    continue;
                }
                
                String val = pair.substring( pos+1 );
                
                res.put(  key, val.trim() );
            }
            
            return res;
        }
        
        return null;
    }
    //----------------------------------------------------------------------------
    private boolean isEncryptedString( String str )
    {
        if( str == null )
            return false;

        return _cipher.isEncryptedString( str );
    }
    //----------------------------------------------------------------------------
    private Sec getSec()
    throws SecDispatcherException
    {
        String location = System.getProperty( SYSTEM_PROPERTY_SEC_LOCATION
                                            , System.getProperty( "user.home" ) + "/.m2/sec.xml"
                                            );
        Sec sec = SecUtil.read( location, true );
        
        if( sec == null )
            throw new SecDispatcherException( "cannot retrieve master password. Please check that "+location+" exists and has data" );
        
        return sec;
    }
    //----------------------------------------------------------------------------
    private String getMaster( Sec sec )
    throws SecDispatcherException
    {
        String master = sec.getMaster();
        
        if( master == null )
            throw new SecDispatcherException( "master password is not set" );
        
        try
        {
            return _cipher.decryptDecorated( master, SYSTEM_PROPERTY_SEC_LOCATION );
        }
        catch ( PlexusCipherException e )
        {
            throw new SecDispatcherException(e);
        }
    }
    //----------------------------------------------------------------------------
    // ***************************************************************
    /**
     * Encrytion helper
     * @throws IOException 
     */

    //---------------------------------------------------------------
    public static void main( String[] args )
    throws Exception
    {
        if( args == null || args.length < 1 )
        {
            System.out.println("usage: Sec [-m|-p]\n-m: encrypt master password\n-p: encrypt password");
            return;
        }
        
        if( "-m".equals( args[0] ) ) 
            show( true );
        else
            show( false );
    }
    //---------------------------------------------------------------
    private static void show( boolean showMaster )
    throws Exception
    {
        System.out.print("Enter password: ");
        byte [] buf = new byte[128];
        
        System.in.read( buf );
        String pass = new String( buf ); 
        System.out.println("\n");
        
        DefaultPlexusCipher dc = new DefaultPlexusCipher();
        DefaultSecDispatcher dd = new DefaultSecDispatcher();
        dd._cipher = dc;
        
        if( showMaster )
            System.out.println( dc.encryptAndDecorate( pass, DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION ) );
        else
        {
            Sec sec = dd.getSec();
            System.out.println( dc.encryptAndDecorate( pass, dd.getMaster(sec) ) );
        }
    }
    //---------------------------------------------------------------
    //---------------------------------------------------------------
}
