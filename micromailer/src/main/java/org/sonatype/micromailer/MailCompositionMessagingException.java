package org.sonatype.micromailer;

/**
 * Exception thrown during composition of email, like bad address, etc.
 * 
 * @author cstamas
 */
public class MailCompositionMessagingException
    extends MailCompositionException
{
    private static final long serialVersionUID = -723342519102045789L;

    public MailCompositionMessagingException( String msg, Throwable ex )
    {
        super( msg, ex );
    }

    public MailCompositionMessagingException( String msg )
    {
        super( msg );
    }
}
