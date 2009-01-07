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

import java.security.MessageDigest;

/**
 * @author AlAg
 */
public class Digester {
    
    
    // -----------------------------------------------------------------------
    public static final String digest( long value ){
        return digest( Long.toString( value ) );
    }
    // -----------------------------------------------------------------------
    public static final String digest( String value ) {

        String digest = null;
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            digest = bytesToHex( messageDigest.digest( value.getBytes() ) );    
        
        } catch( Exception e ){
            e.printStackTrace();
        }
        
        return digest;
    }
    
    
    // ===============================================================================
    /** quick array to convert byte values to hex codes */
    private final static char[] HEX = {'0','1','2','3','4','5','6','7','8', '9','a','b','c','d','e','f'};

    /**
     * This utility method is passed an array of bytes. It returns
     * this array as a String in hexadecimal format. This is used
     * internally by <code>digest()</code>. Data is returned in
     * the format specified by the Jabber protocol.
     */
    private static String bytesToHex( byte[] data ){
        StringBuffer retval = new StringBuffer();
        for(int i=0;i<data.length;i++) {
            retval.append(HEX[ (data[i]>>4)&0x0F ]);
            retval.append(HEX[ data[i]&0x0F ]);
        }
        return retval.toString();
    }
    
}

