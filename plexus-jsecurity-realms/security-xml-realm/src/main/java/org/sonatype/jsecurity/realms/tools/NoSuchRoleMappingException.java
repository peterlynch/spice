package org.sonatype.jsecurity.realms.tools;

public class NoSuchRoleMappingException
    extends Exception
{

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -8368148376838186349L;

    public NoSuchRoleMappingException()
    {
        super();
    }

    public NoSuchRoleMappingException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NoSuchRoleMappingException( String message )
    {
        super( message );
    }

    public NoSuchRoleMappingException( Throwable cause )
    {
        super( cause );
    }

}
