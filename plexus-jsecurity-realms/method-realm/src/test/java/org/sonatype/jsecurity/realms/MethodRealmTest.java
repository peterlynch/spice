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
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.DefaultConfigurationManager;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityProperty;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;

public class MethodRealmTest
    extends PlexusTestCase
{
    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";
    public static final String PLEXUS_STATIC_SECURITY = "static-security-resource";
    
    private static final String SECURITY_CONFIG_FILE_PATH = getBasedir() + "/target/jsecurity/security.xml"; 
    
    private File configFile = new File( SECURITY_CONFIG_FILE_PATH );
    
    private XmlMethodAuthorizingRealm realm;
    
    private DefaultConfigurationManager configurationManager;
    
    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        
        context.put( PLEXUS_SECURITY_XML_FILE, SECURITY_CONFIG_FILE_PATH );
        context.put( PLEXUS_STATIC_SECURITY, "" );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        realm = ( XmlMethodAuthorizingRealm ) lookup( Realm.class, "XmlMethodAuthorizingRealm" );
        
        configurationManager = ( DefaultConfigurationManager ) lookup( ConfigurationManager.class );
        
        configurationManager.clearCache();
        
        configFile.delete();
    }
    
    public void testAuthorization()
        throws Exception
    {
        buildTestAuthorizationConfig();
        
        // Fails because the configuration requirement in SecurityXmlRealm isn't initialized
        // thus NPE
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
    
    private void buildTestAuthorizationConfig() throws InvalidConfigurationException
    {
        SecurityProperty permissionProp = new SecurityProperty();
        permissionProp.setKey( "permission" );
        permissionProp.setValue( "app:config" );
        
        SecurityProperty methodProp = new SecurityProperty();
        methodProp.setKey( "method" );
        methodProp.setValue( "read" );
        
        SecurityPrivilege priv = new SecurityPrivilege();
        priv.setId( "priv" );
        priv.setName( "somepriv" );
        priv.setType( "method" );
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
        user.setEmail( "dummyemail" );
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
