package org.sonatype.jsecurity.realms.tools;

import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;

public class ResourceMergingConfigurationManagerTest
    extends PlexusTestCase
{
    private ConfigurationManager manager;
    
    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        
        context.put( "security-xml-file", "target/test-classes/org/sonatype/jsecurity/configuration/static-merging/security.xml" );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        manager = ( ConfigurationManager ) lookup( ConfigurationManager.class, "resourceMerging" );
    }
    
    public void testMerging()
        throws Exception
    {
        List<SecurityPrivilege> privs = manager.listPrivileges();
        
        assertEquals( 4, privs.size() );
        
        SecurityPrivilege priv = manager.readPrivilege( "1" );        
        assertTrue( priv != null );
        
        priv = manager.readPrivilege( "2" );
        assertTrue( priv != null );
        
        priv = manager.readPrivilege( "3" );
        assertTrue( priv != null );
        
        priv = manager.readPrivilege( "4" );
        assertTrue( priv != null );
    }
}
