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
package org.sonatype.ldaptestsuite;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.directory.server.constants.ServerDNConstants;

public class SaslLdapTestEnvironmentTest extends AbstractLdapTestEnvironment
{

    
    /**
     * Tests to make sure DIGEST-MD5 binds below the RootDSE work.
     */
    public void testSaslDigestMd5Bind() throws Exception
    {
        
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
        env.put( Context.PROVIDER_URL, "ldap://localhost:12345");

        env.put( Context.SECURITY_AUTHENTICATION, "DIGEST-MD5" );
//        env.put( Context.SECURITY_PRINCIPAL, "admin" );
        env.put( Context.SECURITY_PRINCIPAL, "tstevens" );
        env.put( Context.SECURITY_CREDENTIALS, "tstevens123" );

        // Specify realm
        env.put( "java.naming.security.sasl.realm", "localhost" );

        // Request privacy protection
        env.put( "javax.security.sasl.qop", "auth-conf" );

        DirContext context = new InitialDirContext( env );

        String[] attrIDs =
            { "uid" };

        Attributes attrs = context.getAttributes( "uid=tstevens,ou=people,o=sonatype", attrIDs );

        String uid = null;

        if ( attrs.get( "uid" ) != null )
        {
            uid = ( String ) attrs.get( "uid" ).get();
        }

        assertEquals( uid, "tstevens" );
    }
    
}
