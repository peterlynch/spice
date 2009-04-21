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

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.model.RoleResource;
import org.sonatype.security.rest.model.RoleResourceRequest;
import org.sonatype.security.rest.model.RoleResourceResponse;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RolePlexusResource" )
public class RolePlexusResource
    extends AbstractRolePlexusResource
{

    public static final String ROLE_ID_KEY = "roleId";

    public RolePlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RoleResourceRequest();
    }

    @Override
    public String getResourceUri()
    {
        return "/roles/{" + ROLE_ID_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/roles/*", "authcBasic,perms[security:roles]" );
    }

    protected String getRoleId( Request request )
    {
        return request.getAttributes().get( ROLE_ID_KEY ).toString();
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RoleResourceResponse result = new RoleResourceResponse();

        try
        {
            result.setData( nexusToRestModel( getPlexusSecurity().readRole( getRoleId( request ) ), request ) );

        }
        catch ( NoSuchRoleException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );

        }
        return result;
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RoleResourceRequest resourceRequest = (RoleResourceRequest) payload;
        RoleResourceResponse resourceResponse = new RoleResourceResponse();

        if ( resourceRequest != null )
        {
            RoleResource resource = resourceRequest.getData();
            
            try
            {
                SecurityRole role = restToNexusModel( getPlexusSecurity().readRole( resource.getId() ), resource );
                
                validateRoleContainment( role );

                getPlexusSecurity().updateRole( role );

                resourceResponse = new RoleResourceResponse();

                resourceResponse.setData( resourceRequest.getData() );
                
                resourceResponse.getData().setUserManaged( !role.isReadOnly() );

                resourceResponse
                    .getData().setResourceURI( createChildReference( request, this, resource.getId() ).toString() );

            }
            catch ( NoSuchRoleException e )
            {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
            }
            catch ( InvalidConfigurationException e )
            {
                // build and throw exception
                handleInvalidConfigurationException( e );
            }
        }
        return resourceResponse;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            getPlexusSecurity().deleteRole( getRoleId( request ) );
        }
        catch ( NoSuchRoleException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

}
