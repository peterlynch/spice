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
package org.sonatype.security.realms;

import java.io.File;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.codehaus.plexus.context.Context;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeDescriptor;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeMethodPropertyDescriptor;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegePermissionPropertyDescriptor;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.DefaultConfigurationManager;
import org.sonatype.security.realms.tools.dao.SecurityPrivilege;
import org.sonatype.security.realms.tools.dao.SecurityProperty;
import org.sonatype.security.realms.tools.dao.SecurityRole;
import org.sonatype.security.realms.tools.dao.SecurityUser;

public class XmlAuthorizingRealmTest
    extends AbstractSecurityTestCase
{
    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";
    
    private static final String SECURITY_CONFIG_FILE_PATH = getBasedir() + "/target/jsecurity/security.xml"; 
    
    private File configFile = new File( SECURITY_CONFIG_FILE_PATH );
    
    private XmlAuthorizingRealm realm;
    
    private DefaultConfigurationManager configurationManager;
    
    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        
        context.put( PLEXUS_SECURITY_XML_FILE, SECURITY_CONFIG_FILE_PATH );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        realm = ( XmlAuthorizingRealm ) lookup( Realm.class, "XmlAuthorizingRealm" );
        
        configurationManager = ( DefaultConfigurationManager ) lookup( ConfigurationManager.class );
        
        configurationManager.clearCache();
        
        configFile.delete();
        
        this.lookup( SecuritySystem.class ).start();
    }
    
    public void testAuthorization()
        throws Exception
    {
        buildTestAuthorizationConfig();
        
        // Fails because the configuration requirement in SecurityXmlRealm isn't initialized
        // thus NPE
        SimplePrincipalCollection principal = new SimplePrincipalCollection( "username", realm.getName() );
        
        Assert.assertTrue( realm.hasRole( principal, "role" ) );
        
        // Verify the permission
        Assert.assertTrue( realm.isPermitted( principal, new WildcardPermission( "app:config:read" ) ) );
        // Verify other method not allowed
        Assert.assertFalse( realm.isPermitted( principal,new WildcardPermission( "app:config:create" ) ) );
        Assert.assertFalse( realm.isPermitted( principal,new WildcardPermission( "app:config:update" ) ) );
        Assert.assertFalse( realm.isPermitted( principal,new WildcardPermission( "app:config:delete" ) ) );
        
        // Verify other permission not allowed
        Assert.assertFalse( realm.isPermitted( principal,new WildcardPermission( "app:ui:read" ) ) );
        Assert.assertFalse( realm.isPermitted( principal,new WildcardPermission( "app:ui:create" ) ) );
        Assert.assertFalse( realm.isPermitted( principal,new WildcardPermission( "app:ui:update" ) ) );
        Assert.assertFalse( realm.isPermitted( principal,new WildcardPermission( "app:ui:delete" ) ) );
    }
    
    private void buildTestAuthorizationConfig() throws InvalidConfigurationException
    {
        SecurityProperty permissionProp = new SecurityProperty();
        permissionProp.setKey( ApplicationPrivilegePermissionPropertyDescriptor.ID );
        permissionProp.setValue( "app:config" );
        
        SecurityProperty methodProp = new SecurityProperty();
        methodProp.setKey( ApplicationPrivilegeMethodPropertyDescriptor.ID );
        methodProp.setValue( "read" );
        
        SecurityPrivilege priv = new SecurityPrivilege();
        priv.setId( "priv" );
        priv.setName( "somepriv" );
        priv.setType( ApplicationPrivilegeDescriptor.TYPE );
        priv.setDescription( "somedescription" );
        priv.addProperty( permissionProp );
        priv.addProperty( methodProp );
        
        configurationManager.createPrivilege( priv );
        
        SecurityRole role = new SecurityRole();
        role.setId( "role" );
        role.setName( "somerole" );
        role.setDescription( "somedescription" );
        role.setSessionTimeout( 60 );
        role.addPrivilege( priv.getId() );
        
        configurationManager.createRole( role );
        
        SecurityUser user = new SecurityUser();
        user.setEmail( "dummyemail@foo" );
        user.setName( "dummyname" );
        user.setStatus( SecurityUser.STATUS_ACTIVE );
        user.setId( "username" );
        user.setPassword( "password" );
        user.addRole( role.getId() );
        
        configurationManager.createUser( user );
        
        configurationManager.save();
    }
    
    public static void assertImplied( Permission testPermission, Collection<Permission> assignedPermissions )
    {
        for ( Permission assignedPermission : assignedPermissions )
        {
            if ( assignedPermission.implies( testPermission ) )
            {
                return;
            }
        }
        Assert.fail( "Expected " + testPermission + " to be implied by " + assignedPermissions );
    }

    public static void assertNotImplied( Permission testPermission, Collection<Permission> assignedPermissions )
    {
        for ( Permission assignedPermission : assignedPermissions )
        {
            if ( assignedPermission.implies( testPermission ) )
            {
                Assert.fail( "Expected " + testPermission + " not to be implied by " + assignedPermission );
            }
        }
    }
}
