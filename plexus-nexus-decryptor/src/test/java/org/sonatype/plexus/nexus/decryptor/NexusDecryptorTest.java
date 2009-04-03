/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.sonatype.plexus.nexus.decryptor;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.plexus.components.sec.dispatcher.PasswordDecryptor;


/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class NexusDecryptorTest
extends PlexusTestCase
{
    PasswordDecryptor _npd;
    
    Map<String, String> _config;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        _config = new HashMap<String, String>();
        
        _config.put( NexusDecryptor.CONFIGURATION_PROPERTY_ENCODE,"true");
        
        _npd = getContainer().lookup( PasswordDecryptor.class, "nexus" );
    }
    
    public void testDecryptBase64()
    throws Exception
    {
        String expected = "{blahblah}";
        
        String [] in  = new String [] { 
                  "blahblah"
        };
        
        for( String s : in )
        {
            String out = _npd.decrypt( s, null, _config );
            
            assertEquals( expected, out );
        }
    }
    
    public void testDecryptRaw()
    throws Exception
    {
        String expected = "blahblahblah";
        
        String [] in  = new String [] { 
                  "YmxhaGJsYWhibGFo"
        };
        
        for( String s : in )
        {
            String out = _npd.decrypt( s, null, null );
            
            assertEquals( expected, out.substring( 2 ) );
        }
    }
}
