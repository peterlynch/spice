package org.sonatype.jsecurity.realms.tools;

public class NoSuchPrivilegeException
    extends Exception
{
    public NoSuchPrivilegeException( String privilegeId )
    {
        super( "Privilege with id='" + privilegeId + "' not found!" );
    }
}
