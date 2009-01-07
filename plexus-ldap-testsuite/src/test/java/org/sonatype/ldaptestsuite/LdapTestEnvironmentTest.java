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
package org.sonatype.ldaptestsuite;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;

import org.apache.directory.server.constants.ServerDNConstants;

public class LdapTestEnvironmentTest extends AbstractLdapTestEnvironment
{

    /**
     * Test that the partition has been correctly created
     * @throws Exception 
     */
    public void testPartition() throws Exception
    {        
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
        env.put( Context.PROVIDER_URL, "ldap://localhost:" + 12345 +"/o=sonatype" );
        env.put( Context.SECURITY_PRINCIPAL, ServerDNConstants.ADMIN_SYSTEM_DN );
        env.put( Context.SECURITY_CREDENTIALS, "secret" );
        env.put( Context.SECURITY_AUTHENTICATION, "simple" );

        // Let's open a connection on this partition
        InitialContext initialContext = new InitialLdapContext( env, null );
        
        // We should be able to read it
        DirContext appRoot = ( DirContext ) initialContext.lookup( "" );
        assertNotNull( appRoot );

        // Let's get the entry associated to the top level
        Attributes attributes = appRoot.getAttributes( "" );
        assertNotNull( attributes );
        assertEquals( "sonatype", ( attributes.get( "o" ) ).get() );

        Attribute attribute = attributes.get( "objectClass" );
        assertNotNull( attribute );
        assertTrue( attribute.contains( "top" ) );
        assertTrue( attribute.contains( "organization" ) );
        // Ok, everything is fine
    }

    
}
