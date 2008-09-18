package org.sonatype.jsecurity.realms.validator;

import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;

public interface ConfigurationValidator
{
    String ROLE = ConfigurationValidator.class.getName();
    
    ValidationResponse validateModel( ValidationRequest request );
    
    ValidationResponse validatePrivilege( ValidationContext ctx, CPrivilege privilege, boolean update );
    
    ValidationResponse validateRoleContainment( ValidationContext ctx );
    
    ValidationResponse validateRole( ValidationContext ctx, CRole role, boolean update );
    
    ValidationResponse validateUser( ValidationContext ctx, CUser user, boolean update );
}
