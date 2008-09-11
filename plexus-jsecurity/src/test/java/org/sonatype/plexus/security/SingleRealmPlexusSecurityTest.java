package org.sonatype.plexus.security;

import org.codehaus.plexus.PlexusTestCase;

public class SingleRealmPlexusSecurityTest
    extends PlexusTestCase
{
    private PlexusSecurity security;
        
    protected void setUp()
        throws Exception
    {
        super.setUp();        
        security = (PlexusSecurity) lookup(PlexusSecurity.class);        
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
