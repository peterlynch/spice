package org.sonatype.jsecurity.locators.users;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = PlexusRoleManager.class )
public class DefaultPlexusRoleManager
    implements PlexusRoleManager
{

    public static final String SOURCE_ALL = "all";

    @Requirement( role = PlexusRoleLocator.class )
    private List<PlexusRoleLocator> locators;

    public Set<String> listRoleIds( String source )
    {
        Set<String> roles = new TreeSet<String>();

        for ( PlexusRoleLocator locator : locators )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                Set<String> locatorRoles = locator.listRoleIds();
                if( locatorRoles != null )
                {
                    roles.addAll( locatorRoles );
                }
            }
        }

        return roles;
    }

    public Set<PlexusRole> listRoles( String source )
    {
        Set<PlexusRole> roles = new TreeSet<PlexusRole>();

        for ( PlexusRoleLocator locator : locators )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                Set<PlexusRole> locatorRoles = locator.listRoles();
                if( locatorRoles != null )
                {
                    roles.addAll( locatorRoles );
                }
            }
        }

        return roles;
    }

}
