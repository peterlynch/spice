package org.sonatype.jsecurity.realms;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;

public class SingleRealmPlexusSecurityTest
    extends PlexusTestCase
{
    private PlexusSecurity security;
    
    public static final String LOCATOR_PROPERTY_FILE = "realm-locator-property-file";
        
    protected void setUp()
        throws Exception
    {
        super.setUp();        
        security = (PlexusSecurity) lookup(PlexusSecurity.class);        
    }
    
    @Override
    protected void customizeContext( Context context )
    {
        context.put( LOCATOR_PROPERTY_FILE, getBasedir() + "/target/test-classes/realm-locator.properties" );
    }
    
    public void testRealmAuthorization()
        throws Exception
    {
    }

    public void testRealmAuthentication()
        throws Exception
    {
    }
}
