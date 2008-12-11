package org.sonatype.jsecurity.locators.users;

import java.util.Set;

public interface PlexusRoleManager
{

    public static final String SOURCE_ALL = "all";
    
    /**
     * Retrieve all PlexusRole objects defined by the PlexusRoleLocator components
     * @return
     */
    Set<PlexusRole> listRoles( String source );
    
    /**
     * Retrieve all roleIds defined by the PlexusRoleLocator components (if managing full object
     * list is to heavy handed)
     * @return
     */
    Set<String> listRoleIds( String source );

}
