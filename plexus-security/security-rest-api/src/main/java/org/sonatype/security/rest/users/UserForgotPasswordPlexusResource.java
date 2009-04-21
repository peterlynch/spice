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
package org.sonatype.security.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.email.NoSuchEmailException;
import org.sonatype.security.rest.model.UserForgotPasswordRequest;
import org.sonatype.security.rest.model.UserForgotPasswordResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "UserForgotPasswordPlexusResource" )
public class UserForgotPasswordPlexusResource
    extends AbstractUserPlexusResource
{

    public UserForgotPasswordPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new UserForgotPasswordRequest();
    }

    @Override
    public String getResourceUri()
    {
        return "/users_forgotpw";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[security:usersforgotpw]" );
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        UserForgotPasswordRequest forgotPasswordRequest = (UserForgotPasswordRequest) payload;

        if ( forgotPasswordRequest != null )
        {
            UserForgotPasswordResource resource = forgotPasswordRequest.getData();

            try
            {
                if ( !isAnonymousUser( resource.getUserId(), request ) )
                {
                    getPlexusSecurity().forgotPassword( resource.getUserId(), resource.getEmail() );

                    response.setStatus( Status.SUCCESS_ACCEPTED );
                }
                else
                {
                    response.setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Anonymous user cannot forget password" );

                    getLogger().debug( "Anonymous user forgot password is blocked" );
                }
            }
            catch ( NoSuchUserException e )
            {
                getLogger().debug( "Invalid Username", e );

                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid Username" );
            }
            catch ( NoSuchEmailException e )
            {
                getLogger().debug( "Invalid E-mail", e );

                response.setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "E-mail address not found" );
            }
        }
        // return null because the status is 202
        return null;
    }

}
