package org.sonatype.appbooter.ctl;

/**
 * Thrown if there is a problem starting/stopping a Service.
 * @see Service
 *
 */
public class AppBooterServiceException
    extends Exception
{

    /**
     * Generated serial version ID.
     */
    private static final long serialVersionUID = -2544659876529142511L;

    public AppBooterServiceException()
    {
        // TODO Auto-generated constructor stub
    }

    public AppBooterServiceException( String message )
    {
        super( message );
        // TODO Auto-generated constructor stub
    }

    public AppBooterServiceException( Throwable cause )
    {
        super( cause );
        // TODO Auto-generated constructor stub
    }

    public AppBooterServiceException( String message, Throwable cause )
    {
        super( message, cause );
        // TODO Auto-generated constructor stub
    }

}
