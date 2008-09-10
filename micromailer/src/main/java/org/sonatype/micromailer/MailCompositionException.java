package org.sonatype.micromailer;

/**
 * Thrown where some problem occurs during composition of mail.
 * 
 * @author cstamas
 */
public abstract class MailCompositionException
    extends Exception
{
    private static final long serialVersionUID = 6740991433709910341L;

    public MailCompositionException( String msg )
    {
        super( msg );
    }

    public MailCompositionException( String msg, Throwable cause )
    {
        super( msg, cause );
    }

}
