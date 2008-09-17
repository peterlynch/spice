package org.sonatype.jsecurity.locators;

import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.realms.FakeRealm;

public class PropertyFileRealmLocatorTest
    extends PlexusTestCase
{
    private RealmLocator locator;
    
    public static final String LOCATOR_PROPERTY_FILE = "realm-locator-property-file";
    
    @Override
    protected void customizeContext( Context context )
    {
        context.put( LOCATOR_PROPERTY_FILE, getBasedir() + "/target/test-classes/realm-locator.properties" );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        locator = ( RealmLocator ) lookup( RealmLocator.class );
    }
    
    public void testLocator()
        throws Exception
    {
        List<Realm> realms = locator.getRealms();
        
        assertTrue( realms.size() == 1);
        
        assertTrue( realms.get( 0 ).getClass().equals( FakeRealm.class ) );
    }
}
