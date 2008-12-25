package org.sonatype.jsecurity.realms.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jsecurity.locators.SecurityXmlPlexusUserLocator;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.CUserRoleMapping;
import org.sonatype.jsecurity.model.Configuration;

@Component( role = ConfigurationValidator.class )
public class DefaultConfigurationValidator
    extends AbstractLogEnabled
    implements ConfigurationValidator
{
    @Requirement
    private ConfigurationIdGenerator idGenerator;

    public ValidationResponse validateModel( ValidationRequest request )
    {
        ValidationResponse response = new ValidationResponse();

        Configuration model = (Configuration) request.getConfiguration();

        ValidationContext context = response.getContext();

        List<CPrivilege> privs = model.getPrivileges();

        if ( privs != null )
        {
            for ( CPrivilege priv : privs )
            {
                response.append( validatePrivilege( context, priv, false ) );
            }
        }

        List<CRole> roles = model.getRoles();

        if ( roles != null )
        {
            for ( CRole role : roles )
            {
                response.append( validateRole( context, role, false ) );
            }
        }

        response.append( validateRoleContainment( context ) );

        List<CUser> users = model.getUsers();

        if ( users != null )
        {
            for ( CUser user : users )
            {
                Set<String> roleIds = new HashSet<String>();
                for ( CUserRoleMapping userRoleMapping : (List<CUserRoleMapping>) model.getUserRoleMappings() )
                {
                    if ( userRoleMapping.getUserId() != null && userRoleMapping.getUserId().equals( user.getId() )
                        && ( SecurityXmlPlexusUserLocator.SOURCE.equals(userRoleMapping.getSource() ) ) )
                    {
                        roleIds.addAll( userRoleMapping.getRoles() );
                    }
                }

                response.append( validateUser( context, user, roleIds, false ) );
            }
        }

        List<CUserRoleMapping> userRoleMappings = model.getUserRoleMappings();
        if ( userRoleMappings != null )
        {
            for ( CUserRoleMapping userRoleMapping : userRoleMappings )
            {
                response.append( this.validateUserRoleMapping( context, userRoleMapping, false ) );
            }
        }

        // summary
        if ( response.getValidationErrors().size() > 0 || response.getValidationWarnings().size() > 0 )
        {
            getLogger().error( "* * * * * * * * * * * * * * * * * * * * * * * * * *" );

            getLogger().error( "Security configuration has validation errors/warnings" );

            getLogger().error( "* * * * * * * * * * * * * * * * * * * * * * * * * *" );

            if ( response.getValidationErrors().size() > 0 )
            {
                getLogger().error( "The ERRORS:" );

                for ( ValidationMessage msg : response.getValidationErrors() )
                {
                    getLogger().error( msg.toString() );
                }
            }

            if ( response.getValidationWarnings().size() > 0 )
            {
                getLogger().error( "The WARNINGS:" );

                for ( ValidationMessage msg : response.getValidationWarnings() )
                {
                    getLogger().error( msg.toString() );
                }
            }

            getLogger().error( "* * * * * * * * * * * * * * * * * * * * *" );
        }
        else
        {
            getLogger().info( "Security configuration validated succesfully." );
        }

        return response;
    }

    public ValidationResponse validatePrivilege( ValidationContext ctx, CPrivilege privilege, boolean update )
    {
        ValidationResponse response = new ValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        ValidationContext context = response.getContext();

        List<String> existingIds = context.getExistingPrivilegeIds();

        if ( existingIds == null )
        {
            context.addExistingPrivilegeIds();

            existingIds = context.getExistingPrivilegeIds();
        }

        if ( !update
            && ( StringUtils.isEmpty( privilege.getId() ) || "0".equals( privilege.getId() ) || ( existingIds
                .contains( privilege.getId() ) ) ) )
        {
            String newId = idGenerator.generateId();

            ValidationMessage message = new ValidationMessage( "id", "Fixed wrong privilege ID from '"
                + privilege.getId() + "' to '" + newId + "'" );
            response.addValidationWarning( message );

            privilege.setId( newId );

            response.setModified( true );
        }

        if ( StringUtils.isEmpty( privilege.getType() ) )
        {
            ValidationMessage message = new ValidationMessage(
                "type",
                "Cannot have an empty type",
                "Privilege cannot have an invalid type" );

            response.addValidationError( message );
        }
        else
        {
            // HACK ALERT
            // This validation is exclusive to MethodRealm, should really be done elsewhere
            if ( privilege.getType().equals( "method" ) || privilege.getType().equals( "target" ) )
            {
                // validate method
                // method is of form ('*' | 'read' | 'create' | 'update' | 'delete' [, method]* )
                // so, 'read' method is correct, but so is also 'create,update,delete'
                // '*' means ALL POSSIBLE value for this "field"
                String method = null;
                String permission = null;
                String repositoryId = null;
                String repositoryTargetId = null;
                String repositoryGroupId = null;

                for ( CProperty property : (List<CProperty>) privilege.getProperties() )
                {
                    if ( property.getKey().equals( "method" ) )
                    {
                        method = property.getValue();
                    }
                    else if ( property.getKey().equals( "permission" ) )
                    {
                        permission = property.getValue();
                    }
                    else if ( property.getKey().equals( "repositoryId" ) )
                    {
                        repositoryId = property.getValue();
                    }
                    else if ( property.getKey().equals( "repositoryTargetId" ) )
                    {
                        repositoryTargetId = property.getValue();
                    }
                    else if ( property.getKey().equals( "repositoryGroupId" ) )
                    {
                        repositoryGroupId = property.getValue();
                    }
                }

                if ( privilege.getType().equals( "method" ) )
                {
                    if ( StringUtils.isEmpty( permission ) )
                    {
                        response.addValidationError( "Permission cannot be empty on a privilege!" );
                    }
                }
                else if ( privilege.getType().equals( "target" ) )
                {
                    if ( StringUtils.isEmpty( repositoryTargetId ) )
                    {
                        ValidationMessage message = new ValidationMessage( "repositoryTargetId", "Privilege ID '"
                            + privilege.getId() + "' requires a repositoryTargetId.", "Repository Target is required." );
                        response.addValidationError( message );
                    }

                    if ( !StringUtils.isEmpty( repositoryId ) && !StringUtils.isEmpty( repositoryGroupId ) )
                    {
                        ValidationMessage message = new ValidationMessage(
                            "repositoryId",
                            "Privilege ID '"
                                + privilege.getId()
                                + "' cannot be assigned to both a group and repository."
                                + "  Either assign a group, a repository or neither (which assigns to ALL repositories).",
                            "Cannot select both a Repository and Repository Group." );
                        response.addValidationError( message );
                    }
                }

                if ( StringUtils.isEmpty( method ) )
                {
                    response.addValidationError( "Method cannot be empty on a privilege!" );
                }
                else
                {
                    String[] methods = null;

                    if ( method.contains( "," ) )
                    {
                        // it is a list of methods
                        methods = method.split( "," );
                    }
                    else
                    {
                        // it is a single method
                        methods = new String[] { method };
                    }

                    boolean valid = true;

                    for ( String singlemethod : methods )
                    {
                        if ( !"create".equals( singlemethod ) && !"delete".equals( singlemethod )
                            && !"read".equals( singlemethod ) && !"update".equals( singlemethod )
                            && !"*".equals( singlemethod ) )
                        {
                            valid = false;

                            break;
                        }
                    }

                    if ( !valid )
                    {
                        ValidationMessage message = new ValidationMessage(
                            "method",
                            "Privilege ID '" + privilege.getId()
                                + "' Method is wrong! (Allowed methods are: create, delete, read and update)",
                            "Invalid method selected." );
                        response.addValidationError( message );
                    }

                }
            }
        }

        if ( StringUtils.isEmpty( privilege.getName() ) )
        {
            ValidationMessage message = new ValidationMessage( "name", "Privilege ID '" + privilege.getId()
                + "' requires a name.", "Name is required." );
            response.addValidationError( message );
        }

        existingIds.add( privilege.getId() );

        return response;
    }

    public ValidationResponse validateRoleContainment( ValidationContext ctx )
    {
        ValidationResponse response = new ValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        ValidationContext context = response.getContext();

        if ( context.getExistingRoleIds() != null )
        {
            for ( String roleId : context.getExistingRoleIds() )
            {
                response.append( isRecursive( roleId, roleId, ctx ) );
            }
        }

        return response;
    }

    private boolean isRoleNameAlreadyInUse( Map<String, String> existingRoleNameMap, CRole role )
    {
        for ( String roleId : existingRoleNameMap.keySet() )
        {
            if ( roleId.equals( role.getId() ) )
            {
                continue;
            }
            if ( existingRoleNameMap.get( roleId ).equals( role.getName() ) )
            {
                return true;
            }
        }
        return false;
    }

    private ValidationResponse isRecursive( String baseRoleId, String roleId, ValidationContext ctx )
    {
        ValidationResponse response = new ValidationResponse();

        List<String> containedRoles = ctx.getRoleContainmentMap().get( roleId );

        for ( String containedRoleId : containedRoles )
        {
            // Only need to do this on the first level
            if ( baseRoleId.equals( roleId ) )
            {
                if ( !ctx.getExistingRoleIds().contains( roleId ) )
                {
                    ValidationMessage message = new ValidationMessage( "roles", "Role ID '" + baseRoleId
                        + "' contains an invalid role", "Role cannot contain invalid role ID '" + roleId + "'." );

                    response.addValidationError( message );
                }
            }

            if ( containedRoleId.equals( baseRoleId ) )
            {
                ValidationMessage message = new ValidationMessage(
                    "roles",
                    "Role ID '" + baseRoleId + "' contains itself through Role ID '" + roleId
                        + "'.  This is not valid.",
                    "Role cannot contain itself recursively (via role ID '" + roleId + "')." );

                response.addValidationError( message );

                break;
            }

            if ( ctx.getExistingRoleIds().contains( containedRoleId ) )
            {
                response.append( isRecursive( baseRoleId, containedRoleId, ctx ) );
            }
            // Only need to do this on the first level
            else if ( baseRoleId.equals( roleId ) )
            {
                ValidationMessage message = new ValidationMessage(
                    "roles",
                    "Role ID '" + roleId + "' contains an invalid role ID '" + containedRoleId + "'.",
                    "Role cannot contain invalid role ID '" + containedRoleId + "'." );

                response.addValidationError( message );
            }
        }

        return response;
    }

    public ValidationResponse validateRole( ValidationContext ctx, CRole role, boolean update )
    {
        ValidationResponse response = new ValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        ValidationContext context = response.getContext();

        List<String> existingIds = context.getExistingRoleIds();

        if ( existingIds == null )
        {
            context.addExistingRoleIds();

            existingIds = context.getExistingRoleIds();
        }

        if ( !update && existingIds.contains( role.getId() ) )
        {
            ValidationMessage message = new ValidationMessage( "id", "Role ID must be unique." );
            response.addValidationError( message );
        }

        if ( update && !existingIds.contains( role.getId() ) )
        {
            ValidationMessage message = new ValidationMessage( "id", "Role ID cannot be changed." );
            response.addValidationError( message );
        }

        if ( !update && ( StringUtils.isEmpty( role.getId() ) || "0".equals( role.getId() ) ) )
        {
            String newId = idGenerator.generateId();

            response.addValidationWarning( "Fixed wrong role ID from '" + role.getId() + "' to '" + newId + "'" );

            role.setId( newId );

            response.setModified( true );
        }

        Map<String, String> existingRoleNameMap = context.getExistingRoleNameMap();

        if ( StringUtils.isEmpty( role.getName() ) )
        {
            ValidationMessage message = new ValidationMessage( "name", "Role ID '" + role.getId()
                + "' requires a name.", "Name is required." );
            response.addValidationError( message );
        }
        else if ( isRoleNameAlreadyInUse( existingRoleNameMap, role ) )
        {
            ValidationMessage message = new ValidationMessage( "name", "Role ID '" + role.getId()
                + "' can't use the name '" + role.getName() + "'.", "Name is already in use." );
            response.addValidationError( message );
        }
        else
        {
            existingRoleNameMap.put( role.getId(), role.getName() );
        }

        if ( !role.getId().matches( "\\S*" ) )
        {
            ValidationMessage message = new ValidationMessage( "id", "Whitespace is not allowed in Role ID '" + role.getId()
                + ".", "Whitespace is not allowed." );
            response.addValidationError( message );
        }
        
        if ( 1 > role.getSessionTimeout() )
        {
            ValidationMessage message = new ValidationMessage(
                "sessionTimeout",
                "Role ID '" + role.getId() + "' requires a Session Timeout greater than 0 minutes.",
                "Enter a session timeout greater than 0 minutes." );
            response.addValidationError( message );
        }

        // No roles or privs
        if ( role.getRoles().size() == 0 && role.getPrivileges().size() == 0 )
        {
            ValidationMessage message = new ValidationMessage( "privileges", "Role ID '" + role.getId()
                + "' is required to contain at least 1 role or privilege.", "One or more roles/privilegs are required." );
            response.addValidationError( message );
        }

        if ( context.getExistingPrivilegeIds() != null )
        {
            List<String> privIds = role.getPrivileges();

            for ( String privId : privIds )
            {
                if ( !context.getExistingPrivilegeIds().contains( privId ) )
                {
                    ValidationMessage message = new ValidationMessage(
                        "privileges",
                        "Role ID '" + role.getId() + "' Invalid privilege id '" + privId + "' found.",
                        "Role cannot contain invalid privilege ID '" + privId + "'." );
                    response.addValidationError( message );
                }
            }
        }

        List<String> roleIds = role.getRoles();

        List<String> containedRoles = context.getRoleContainmentMap().get( role.getId() );

        if ( containedRoles == null )
        {
            containedRoles = new ArrayList();
            context.getRoleContainmentMap().put( role.getId(), containedRoles );
        }

        for ( String roleId : roleIds )
        {
            if ( roleId.equals( role.getId() ) )
            {
                ValidationMessage message = new ValidationMessage( "roles", "Role ID '" + role.getId()
                    + "' cannot contain itself.", "Role cannot contain itself." );
                response.addValidationError( message );
            }
            else if ( context.getRoleContainmentMap() != null )
            {
                containedRoles.add( roleId );
            }
        }

        // It is expected that a full context is built upon update
        if ( update )
        {
            response.append( isRecursive( role.getId(), role.getId(), context ) );
        }

        existingIds.add( role.getId() );

        return response;
    }

    public ValidationResponse validateUser( ValidationContext ctx, CUser user, Set<String> roles, boolean update )
    {
        ValidationResponse response = new ValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        ValidationContext context = response.getContext();

        List<String> existingIds = context.getExistingUserIds();

        if ( existingIds == null )
        {
            context.addExistingUserIds();

            existingIds = context.getExistingUserIds();
        }

        Map<String, String> existingEmailMap = context.getExistingEmailMap();

        if ( !update && ( StringUtils.isEmpty( user.getId() ) || existingIds.contains( user.getId() ) ) )
        {
            ValidationMessage message = new ValidationMessage( "userId", "User ID '" + user.getId()
                + "' is invalid.  It is either empty or already in use.", "User Id is required and must be unique." );
            response.addValidationError( message );
        }

        if ( StringUtils.isEmpty( user.getName() ) )
        {
            ValidationMessage message = new ValidationMessage( "name", "User ID '" + user.getId()
                + "' has no Name.  This is a required field.", "Name is required." );
            response.addValidationError( message );
        }

        if ( StringUtils.isEmpty( user.getPassword() ) )
        {
            ValidationMessage message = new ValidationMessage( "password", "User ID '" + user.getId()
                + "' has no password.  This is a required field.", "Password is required." );
            response.addValidationError( message );
        }

        if ( StringUtils.isEmpty( user.getEmail() ) )
        {
            ValidationMessage message = new ValidationMessage( "email", "User ID '" + user.getId()
                + "' has no email address", "Email address is required." );
            response.addValidationError( message );
        }
        else
        {
            existingEmailMap.put( user.getId(), user.getEmail() );
        }

        if ( !CUser.STATUS_ACTIVE.equals( user.getStatus() ) && !CUser.STATUS_DISABLED.equals( user.getStatus() ) )
        {
            ValidationMessage message = new ValidationMessage( "status", "User ID '" + user.getId()
                + "' has invalid status '" + user.getStatus() + "'.  (Allowed values are: " + CUser.STATUS_ACTIVE
                + " and " + CUser.STATUS_DISABLED + ")", "Invalid Status selected." );
            response.addValidationError( message );
        }

        if ( context.getExistingRoleIds() != null && context.getExistingUserRoleMap() != null )
        {

            if ( roles == null || roles.isEmpty() )
            {
                ValidationMessage message = new ValidationMessage( "roles", "User ID '" + user.getId()
                    + "' has no roles assigned.", "User requires one or more roles." );
                response.addValidationError( message );
            }
            else
            {
                for ( String roleId : roles )
                {
                    if ( !context.getExistingRoleIds().contains( roleId ) )
                    {
                        ValidationMessage message = new ValidationMessage( "roles", "User ID '" + user.getId()
                            + "' Invalid role id '" + roleId + "' found.", "User cannot contain invalid role ID '"
                            + roleId + "'." );
                        response.addValidationError( message );
                    }
                }
            }
        }

        if ( !StringUtils.isEmpty( user.getId() ) )
        {
            existingIds.add( user.getId() );
        }

        return response;
    }

    public ValidationResponse validateUserRoleMapping( ValidationContext context, CUserRoleMapping userRoleMapping,
        boolean update )
    {
        ValidationResponse response = new ValidationResponse();

        // ID must be not empty
        if ( StringUtils.isEmpty( userRoleMapping.getUserId() ) )
        {
            ValidationMessage message = new ValidationMessage( "userId", "UserRoleMapping has no userId."
                + "  This is a required field.", "UserId is required." );
            response.addValidationError( message );
        }

        // source must be not empty
        if ( StringUtils.isEmpty( userRoleMapping.getSource() ) )
        {
            ValidationMessage message = new ValidationMessage( "source", "User Role Mapping for user '"
                + userRoleMapping.getUserId() + "' has no source.  This is a required field.", "UserId is required." );
            response.addValidationError( message );
        }

        List<String> roles = userRoleMapping.getRoles();
        // all roles must be real
        if ( context.getExistingRoleIds() != null && context.getExistingUserRoleMap() != null )
        {

            if ( roles == null || roles.isEmpty() )
            {
                ValidationMessage message = new ValidationMessage( "roles", "User Role Mapping for user '"
                    + userRoleMapping.getUserId() + "' has no roles assigned.", "User requires one or more roles." );
                response.addValidationError( message );
            }
            else
            {
                for ( String roleId : roles )
                {
                    if ( !context.getExistingRoleIds().contains( roleId ) )
                    {
                        ValidationMessage message = new ValidationMessage(
                            "roles",
                            "User Role Mapping for user '" + userRoleMapping.getUserId() + "' Invalid role id '"
                                + roleId + "' found.",
                            "User cannot contain invalid role ID '" + roleId + "'." );
                        response.addValidationError( message );
                    }
                }
            }
        }

        return response;
    }
}
