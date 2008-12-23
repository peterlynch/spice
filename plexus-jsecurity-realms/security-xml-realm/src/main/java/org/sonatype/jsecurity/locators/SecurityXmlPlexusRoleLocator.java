package org.sonatype.jsecurity.locators;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusRoleLocator;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;

/**
 * PlexusRoleLocator that wraps roles from security-xml-realm.
 */
@Component( role = PlexusRoleLocator.class )
public class SecurityXmlPlexusRoleLocator
    implements PlexusRoleLocator
{

    public static final String SOURCE = "default";

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    public String getSource()
    {
        return SOURCE;
    }

    public Set<String> listRoleIds()
    {
        Set<String> roleIds = new TreeSet<String>();
        List<SecurityRole> secRoles = this.configuration.listRoles();

        for ( SecurityRole securityRole : secRoles )
        {
            roleIds.add( securityRole.getId() );
        }

        return roleIds;
    }

    public Set<PlexusRole> listRoles()
    {
        Set<PlexusRole> roles = new TreeSet<PlexusRole>();
        List<SecurityRole> secRoles = this.configuration.listRoles();

        for ( SecurityRole securityRole : secRoles )
        {
            roles.add( this.toPlexusRole( securityRole ) );
        }

        return roles;
    }

    protected PlexusRole toPlexusRole( SecurityRole role )
    {

        PlexusRole plexusRole = new PlexusRole();

        plexusRole.setRoleId( role.getId() );
        plexusRole.setName( role.getName() );
        plexusRole.setSource( SOURCE );

        return plexusRole;

    }

}
