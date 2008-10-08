package org.sonatype.jsecurity.web;

public class SecurityConfigurationException
    extends Exception
{
    private static final long serialVersionUID = 8755737915569964706L;

    public SecurityConfigurationException( String message )
    {
        super( message );
    }

    public SecurityConfigurationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
