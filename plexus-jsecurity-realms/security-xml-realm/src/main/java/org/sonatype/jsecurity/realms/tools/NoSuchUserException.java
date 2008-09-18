package org.sonatype.jsecurity.realms.tools;

public class NoSuchUserException
    extends Exception
{
    public NoSuchUserException()
    {
        super( "User not found!" );
    }

    public NoSuchUserException( String userId )
    {
        super( "User with id='" + userId + "' not found!" );
    }
}
