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
package net.java.dev.openim.tools;


/**
 * @author AlAg
 */
public class JIDParser {
    
    //-------------------------------------------------------------------------
    public static final String getJID( final String jidAndRes ) {
        return getName( jidAndRes )+'@'+getHostname( jidAndRes );
    }
    //-------------------------------------------------------------------------
    public static final String getHostname( final String jidAndRes ) {
        String hostname = null;
        
            int index = jidAndRes.indexOf( '@' );
            if( index > 0 ){
                hostname = jidAndRes.substring( index + 1 );
                index = hostname.indexOf( '/' );
                if( index > 0 ){
                    hostname = hostname.substring( 0, index );
                }
                hostname = hostname.toLowerCase();
            }
        
        return hostname;
    }
    
    //-------------------------------------------------------------------------
    public static final String getNameAndRessource( final String jidAndRes ) {
        String nameAndRes = null;
        
        if( jidAndRes != null ){
            int index = jidAndRes.indexOf( '@' );
            if( index > 0 ){
                nameAndRes = jidAndRes.substring( 0, index ).toLowerCase();
                index = jidAndRes.lastIndexOf( '/' );
                if( index > 0 ){
                    nameAndRes += jidAndRes.substring( index );
                }
            }
        }
        
        return nameAndRes;
    }
    //-------------------------------------------------------------------------
    public static final String getName( final String jidAndRes ) {
        String name = jidAndRes;
    
        if( jidAndRes != null ){

            int index = jidAndRes.lastIndexOf( '/' );
            if( index > 0 ){
                name = jidAndRes.substring( 0, index );
            }

            index = name.indexOf( '@' );
            if( index > 0 ){
                name = name.substring( 0, index );
            } 
            
            if( name != null ){
                name = name.toLowerCase();
            }
        }
        
        return name;
    }

    
    
    
}

