package org.sonatype.security.realms.tools;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.PlexusSecurity;
import org.sonatype.security.configuration.SecurityRestStaticSecurityResource;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.Configuration;

public class AllPermissionsAreDefinedTest
    extends PlexusTestCase
{

    public List<PlexusResource> getPlexusResources()
        throws ComponentLookupException
    {
        return this.getContainer().lookupList( PlexusResource.class );
    }

    @SuppressWarnings( "unchecked" )
    public void testEnsurePermissions()
        throws Exception
    {   
        Set<String> restPerms = new HashSet<String>();
        Set<String> staticPerms = new HashSet<String>();

        for ( PlexusResource plexusResource : this.getPlexusResources() )
        {
            PathProtectionDescriptor ppd = plexusResource.getResourceProtection();

            String expression = ppd.getFilterExpression();
            if ( expression.contains( "[" ) )
            {
                String permission = ppd.getFilterExpression().substring(
                    expression.indexOf( '[' ) + 1,
                    expression.indexOf( ']' ) );
                restPerms.add( permission );
            }
        }

        // now we have a list of permissions, we need to make sure all of these are in the static security xml.

        SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();
        String staticSecurityPath = new SecurityRestStaticSecurityResource().getResourcePath();
        InputStream configStream = this.getClass().getResourceAsStream( staticSecurityPath );
        Configuration staticConfig = reader.read( configStream );

        List<CPrivilege> privs = staticConfig.getPrivileges();
        for ( CPrivilege privilege : privs )
        {
            staticPerms.add( this.getPermssionFromPrivilege( privilege ) );
        }

        // make sure everything in the restPerms is in the staticPerms
        for ( String perm : restPerms )
        {
            Assert.assertTrue( "Permission: " + perm + " is missing from " + staticSecurityPath, staticPerms
                .contains( perm ) );
        }

    }

    private String getPermssionFromPrivilege( CPrivilege privilege )
    {
        for ( Iterator<CProperty> iter = privilege.getProperties().iterator(); iter.hasNext(); )
        {
            CProperty prop = iter.next();
            if ( prop.getKey().equals( "permission" ) )
            {
                return prop.getValue();
            }
        }
        return null;
    }
}
