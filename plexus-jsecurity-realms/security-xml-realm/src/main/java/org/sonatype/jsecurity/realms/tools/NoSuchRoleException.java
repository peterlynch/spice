package org.sonatype.jsecurity.realms.tools;

public class NoSuchRoleException
    extends Exception
{
    public NoSuchRoleException( String roleId )
    {
        super( "Role with id='" + roleId + "' not found!" );
    }
}
