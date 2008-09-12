package org.sonatype.jsecurity.realms;

import java.io.File;
import java.util.Collection;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.DefaultConfigurationManager;
import org.sonatype.jsecurity.realms.tools.StringDigester;

public class MethodRealmTest
    extends PlexusTestCase
{
    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";
    
    private static final String SECURITY_CONFIG_FILE_PATH = getBasedir() + "/target/jsecurity/security.xml"; 
    
    private File configFile = new File( SECURITY_CONFIG_FILE_PATH );
    
    private MethodRealm realm;
    
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
        
        realm = ( MethodRealm ) lookup( Realm.class, "MethodRealm" );
        
        configurationManager = ( DefaultConfigurationManager ) lookup( ConfigurationManager.ROLE );
        
        configurationManager.clearCache();
        
        configFile.delete();
    }
    
    public void testAuthorization()
        throws Exception
    {
        buildTestAuthorizationConfig();
        
        AuthorizationInfo ai = realm.getAuthorizationInfo( new SimplePrincipalCollection( "username", realm.getName() ) );
        
        assertEquals( ai.getRoles().size(), 1 );
        
        assertEquals( ai.getRoles().iterator().next(), "role" );
        
        Collection<Permission> permissions = ai.getObjectPermissions();
        
        // Verify the permission
        assertImplied( new WildcardPermission( "app:config:read" ), permissions );
        // Verify other method not allowed
        assertNotImplied( new WildcardPermission( "app:config:create" ), permissions );
        assertNotImplied( new WildcardPermission( "app:config:update" ), permissions );
        assertNotImplied( new WildcardPermission( "app:config:delete" ), permissions );
        
        // Verify other permission not allowed
        assertNotImplied( new WildcardPermission( "app:ui:read" ), permissions );
        assertNotImplied( new WildcardPermission( "app:ui:create" ), permissions );
        assertNotImplied( new WildcardPermission( "app:ui:update" ), permissions );
        assertNotImplied( new WildcardPermission( "app:ui:delete" ), permissions );
    }
    
    private void buildTestAuthorizationConfig()
    {
        CProperty permissionProp = new CProperty();
        permissionProp.setKey( "permission" );
        permissionProp.setValue( "app:config" );
        
        CProperty methodProp = new CProperty();
        methodProp.setKey( "method" );
        methodProp.setValue( "read" );
        
        CPrivilege priv = new CPrivilege();
        priv.setId( "priv" );
        priv.setName( "somepriv" );
        priv.setDescription( "somedescription" );
        priv.addProperty( permissionProp );
        priv.addProperty( methodProp );
        
        configurationManager.createPrivilege( priv );
        
        CRole role = new CRole();
        role.setId( "role" );
        role.setName( "somerole" );
        role.setDescription( "somedescription" );
        role.setSessionTimeout( 60 );
        role.addPrivilege( priv.getId() );
        
        configurationManager.createRole( role );
        
        CUser user = new CUser();
        user.setEmail( "dummyemail" );
        user.setName( "dummyname" );
        user.setStatus( CUser.STATUS_ACTIVE );
        user.setId( "username" );
        user.setPassword( StringDigester.getSha1Digest( "password" ) );
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
        fail( "Expected " + testPermission + " to be implied by " + assignedPermissions );
    }

    public static void assertNotImplied( Permission testPermission, Collection<Permission> assignedPermissions )
    {
        for ( Permission assignedPermission : assignedPermissions )
        {
            if ( assignedPermission.implies( testPermission ) )
            {
                fail( "Expected " + testPermission + " not to be implied by " + assignedPermission );
            }
        }
    }
}
