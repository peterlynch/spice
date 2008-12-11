package org.sonatype.jsecurity.realms.validator;

import java.util.Set;

import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.CUserRoleMapping;

public interface ConfigurationValidator
{
    ValidationResponse validateModel( ValidationRequest request );
    
    ValidationResponse validatePrivilege( ValidationContext ctx, CPrivilege privilege, boolean update );
    
    ValidationResponse validateRoleContainment( ValidationContext ctx );
    
    ValidationResponse validateRole( ValidationContext ctx, CRole role, boolean update );
    
    ValidationResponse validateUser( ValidationContext ctx, CUser user, Set<String> roles, boolean update );
    
    ValidationResponse validateUserRoleMapping( ValidationContext ctx, CUserRoleMapping userRoleMapping, boolean update );
}
