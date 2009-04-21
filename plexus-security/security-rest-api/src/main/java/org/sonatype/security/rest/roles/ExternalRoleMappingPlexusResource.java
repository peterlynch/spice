/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.rest.roles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.locators.SecurityXmlPlexusRoleLocator;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusRoleManager;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.model.ExternalRoleMappingResourceResponse;
import org.sonatype.security.rest.model
			.ExternalRoleMappingResource;

@Component( role = PlexusResource.class, hint = "ExternalRoleMappingPlexusResource" )
public class ExternalRoleMappingPlexusResource
    extends AbstractRolePlexusResource
{
    @Requirement
    private PlexusRoleManager roleManager;

    public static final String SOURCE_ID_KEY = "sourceId";

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/external_role_map/*", "authcBasic,perms[security:roles]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/external_role_map/{"+ SOURCE_ID_KEY +"}";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String source = this.getSourceId( request );
        
        // get roles for the source
        Set<PlexusRole> roles = this.roleManager.listRoles( source );
        
        if(roles == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Role Source '"+ source+"' could not be found.");
        }
        
        Set<PlexusRole> defaultRoles = this.roleManager.listRoles( SecurityXmlPlexusRoleLocator.SOURCE );
        
        Map<PlexusRole, Set<PlexusRole>> roleMap = new HashMap<PlexusRole, Set<PlexusRole>>();
        
        for ( PlexusRole defaultRole : defaultRoles )
        {
            for ( PlexusRole role : roles )
            {
                // if the roleId matches (and the source doesn't)
                if( !StringUtils.equals( defaultRole.getSource(), role.getSource() ) && StringUtils.equals( defaultRole.getRoleId(), role.getRoleId() ) )
                {   
                    Set<PlexusRole> mappedRoles = roleMap.get( defaultRole );
                    // if we don't have any currently mapped roles, add it to the map,
                    // if we do then just add to the set
                    
                    if( mappedRoles == null)
                    {
                        mappedRoles = new HashSet<PlexusRole>();
                        mappedRoles.add( role );
                        roleMap.put( defaultRole, mappedRoles );
                    }
                    else
                    {
                        // just add this new role to the current set
                        mappedRoles.add( role );
                    }
                    
                    roleMap.put( defaultRole, mappedRoles );
                }
            }
        }
        
        // now put this in a resource
        ExternalRoleMappingResourceResponse result = new ExternalRoleMappingResourceResponse();
        
        for ( PlexusRole defaultRole : roleMap.keySet() )
        {
            ExternalRoleMappingResource resource = new ExternalRoleMappingResource();
            result.addData( resource );
            resource.setDefaultRole( this.nexusToRestModel( defaultRole ) );
            
            for ( PlexusRole mappedRole : roleMap.get( defaultRole ) )
            {
                resource.addMappedRole( this.nexusToRestModel( mappedRole ) );
            }
        }

        return result;
    }
    
    protected String getSourceId( Request request )
    {
        return request.getAttributes().get( SOURCE_ID_KEY ).toString();
    }
}
