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
package org.sonatype.nexus.rest.authentication;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.model.AuthenticationLoginResource;
import org.sonatype.security.rest.model.AuthenticationLoginResourceResponse;

/**
 * The login resource handler. It creates a user token.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "LoginPlexusResource" )
public class LoginPlexusResource
    extends AbstractUIPermissionCalculatingPlexusResource
{
    @Requirement(hint="additinalRoles")
    private PlexusUserManager userManager;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/authentication/login";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[security:authentication]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        AuthenticationLoginResource resource = new AuthenticationLoginResource();

        resource.setClientPermissions( getClientPermissionsForCurrentUser( request ) );

        AuthenticationLoginResourceResponse result = new AuthenticationLoginResourceResponse();
        
        String username = resource.getClientPermissions().getLoggedInUsername();
        
        if( StringUtils.isNotEmpty( username ))
        {
            // look up the realm of the user
            PlexusUser user = userManager.getUser( username );
            String source = (user != null) ? user.getSource() : null;
            resource.getClientPermissions().setLoggedInUserSource( source);
        }

        result.setData( resource );

        return result;
    }

}
