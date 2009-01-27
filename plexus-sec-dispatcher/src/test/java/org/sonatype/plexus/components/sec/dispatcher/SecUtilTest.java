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

package org.sonatype.plexus.components.sec.dispatcher;

import java.io.FileWriter;
import java.util.Map;

import org.sonatype.plexus.components.sec.dispatcher.model.Config;
import org.sonatype.plexus.components.sec.dispatcher.model.ConfigProperty;
import org.sonatype.plexus.components.sec.dispatcher.model.Sec;
import org.sonatype.plexus.components.sec.dispatcher.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PBECipher;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecUtil;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class SecUtilTest
extends TestCase
{
    String _pw = "{jSMOWnoPFgsHVpMvz5VrIt5kRbzGpI8u+9EF1iFQyJQ=}";
    
    String _clear = "testtest";
    
    String _encrypted = "{8+qG0C/CnRsH+45rqY50rWb1cMILU4zu9K/sZ8omYzs=}";
    
    String _confName = "cname";
    
    String _propName = "pname";
    
    String _propVal = "pval";
    
    protected void setUp()
    throws Exception
    {
        System.setProperty( DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, "./target/sec.xml" );
        
//PBECipher c = new PBECipher();
//System.out.println(_clear+" -> "+c.encrypt64( _clear, "testtest" ));

        
        Sec sec = new Sec();
        
        sec.setRelocation( "./target/sec1.xml" );
        new SecurityConfigurationXpp3Writer().write( new FileWriter("./target/sec.xml"), sec );
        
        sec.setRelocation( null );
        sec.setMaster( _pw );
        
        ConfigProperty cp = new ConfigProperty();
        cp.setName( _propName );
        cp.setValue( _propVal );
        
        Config conf = new Config();
        conf.setName( _confName );
        conf.addProperty( cp );
        
        sec.addConfiguration( conf );
        
        new SecurityConfigurationXpp3Writer().write( new FileWriter("./target/sec1.xml"), sec );
    }

    public void testRead()
    throws Exception
    {
        Sec sec = SecUtil.read( "./target/sec.xml", true );

        assertNotNull( sec );

        assertEquals( _pw, sec.getMaster() );
        
        Map conf = SecUtil.getConfig( sec, _confName );
        
        assertNotNull( conf );
        
        assertNotNull( conf.get( _propName ) );
        
        assertEquals( _propVal, conf.get( _propName ) );
    }


    public void testDecrypt()
    throws Exception
    {
        DefaultSecDispatcher sd = new DefaultSecDispatcher();
        sd._cipher = new DefaultPlexusCipher();
        
        String pass = sd.decrypt( _encrypted, null, null, null );
        
        assertNotNull( pass );
        
        assertEquals( _clear, pass );
    }
}
