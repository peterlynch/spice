package org.sonatype.security.rest;

import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.sonatype.security.PlexusSecurity;
import org.sonatype.security.rest.model.PlexusRoleResource;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.UserResource;

public abstract class AbstractSecurityPlexusResource extends AbstractPlexusResource
{

    @Requirement
    private PlexusSecurity plexusSecurity;

    protected PlexusSecurity getPlexusSecurity()
    {
        return plexusSecurity;
    }
    
    protected ErrorResponse getErrorResponse( String id, String msg )
    {
        ErrorResponse ner = new ErrorResponse();
        ErrorMessage ne = new ErrorMessage();
        ne.setId( id );
        ne.setMsg( msg );
        ner.addError( ne );
        return ner;
    }
    
    protected void handleInvalidConfigurationException(
        org.sonatype.jsecurity.realms.tools.InvalidConfigurationException e )
        throws PlexusResourceException
    {
        getLogger().warn( "Configuration error!", e );

        ErrorResponse nexusErrorResponse;

        org.sonatype.jsecurity.realms.validator.ValidationResponse vr = e.getValidationResponse();

        if ( vr != null && vr.getValidationErrors().size() > 0 )
        {
            org.sonatype.jsecurity.realms.validator.ValidationMessage vm = vr.getValidationErrors().get( 0 );
            nexusErrorResponse = getErrorResponse( vm.getKey(), vm.getShortMessage() );
        }
        else
        {
            nexusErrorResponse = getErrorResponse( "*", e.getMessage() );
        }

        throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", nexusErrorResponse );
    }
    
    protected UserResource nexusToRestModel( SecurityUser user, Request request )
    {
        UserResource resource = new UserResource();
        resource.setEmail( user.getEmail() );
        resource.setName( user.getName() );
        resource.setStatus( user.getStatus() );
        resource.setUserId( user.getId() );
        resource.setResourceURI( this.createChildReference( request, this, resource.getUserId() ).toString() );
        resource.setUserManaged( !user.isReadOnly() );

        for ( String roleId : user.getRoles() )
        {
            resource.addRole( roleId );
        }

        return resource;
    }

    protected SecurityUser restToNexusModel( SecurityUser user, UserResource resource )
    {
        if ( user == null )
        {
            user = new SecurityUser();
        }

        user.setEmail( resource.getEmail() );
        user.setName( resource.getName() );
        user.setStatus( resource.getStatus() );
        user.setId( resource.getUserId() );

        user.getRoles().clear();
        for ( String roleId : (List<String>) resource.getRoles() )
        {
            user.addRole( roleId );
        }

        return user;
    }
    
    protected PlexusUserResource nexusToRestModel( PlexusUser user )
    {
        PlexusUserResource resource = new PlexusUserResource();
        
        resource.setUserId( user.getUserId() );
        resource.setSource( user.getSource() );
        resource.setName( user.getName() );
        resource.setEmail( user.getEmailAddress() );
        
        for ( PlexusRole role : user.getRoles() )
        {   
            resource.addRole( this.nexusToRestModel( role ) );
        }
        
        return resource;
    }
    
    protected PlexusRoleResource nexusToRestModel( PlexusRole role )
    {
        PlexusRoleResource roleResource = new PlexusRoleResource();
        roleResource.setRoleId( role.getRoleId() );
        roleResource.setName( role.getName() );
        roleResource.setSource( role.getSource() );
        
        return roleResource;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Centralized, since this is the only "dependent" stuff that relies on knowledge where restlet.Application is
     * mounted (we had a /service => / move).
     * 
     * @param request
     * @return
     */
    protected Reference getContextRoot( Request request )
    {
        Reference result = null;

//        if ( getNexus().isForceBaseUrl() && getNexus().getBaseUrl() != null )
//        {
//            result = new Reference( getNexus().getBaseUrl() );
//        }
//        else
//        {
            result = request.getRootRef();
//        }

        // fix for when restlet is at webapp root
        if ( StringUtils.isEmpty( result.getPath() ) )
        {
            result.setPath( "/" );
        }

        return result;
    }
    
    private Reference updateBaseRefPath( Reference reference )
    {
        if ( reference.getBaseRef().getPath() == null )
        {
            reference.getBaseRef().setPath( "/" );
        }
        else if ( !reference.getBaseRef().getPath().endsWith( "/" ) )
        {
            reference.getBaseRef().setPath( reference.getBaseRef().getPath() + "/" );
        }
        
        return reference;
    }
    
    protected Reference createChildReference( Request request, PlexusResource resource, String childPath )
    {
        String uriPart = request.getResourceRef().getTargetRef().toString().substring(
            request.getRootRef().getTargetRef().toString().length() );
        
        // trim leading slash
        if ( uriPart.startsWith( "/" ) )
        {
            uriPart = uriPart.substring( 1 );
        }
        
        Reference result = updateBaseRefPath( new Reference( getContextRoot( request ),  uriPart ) ).addSegment( childPath );

        if ( result.hasQuery() )
        {
            result.setQuery( null );
        }

        return result.getTargetRef();
    }
    
}