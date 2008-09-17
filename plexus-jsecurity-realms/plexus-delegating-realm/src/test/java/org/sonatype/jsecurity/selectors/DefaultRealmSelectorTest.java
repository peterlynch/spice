package org.sonatype.jsecurity.selectors;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.realms.FakeRealm;

public class DefaultRealmSelectorTest
    extends PlexusTestCase
{
    public static final String LOCATOR_PROPERTY_FILE = "realm-locator-property-file";
    
    private RealmSelector selector;
    
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
        
        selector = ( RealmSelector ) lookup( RealmSelector.class );
    }
    
    public void testSelector()
        throws Exception
    {
        RealmCriteria criteria = new RealmCriteria();
        
        criteria.setName( FakeRealm.class.getName() );
        
        Realm selected = selector.selectRealm( criteria );
        
        assertTrue( selected != null );
        assertTrue( selected.getName().equals( FakeRealm.class.getName() ) );
    }
}
