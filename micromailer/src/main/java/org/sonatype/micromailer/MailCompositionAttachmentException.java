package org.sonatype.micromailer;

/**
 * Thrown where some problem occured with mail attachment during the composition of the mail.
 * 
 * @author cstamas
 */
public class MailCompositionAttachmentException
    extends MailCompositionException
{
    private static final long serialVersionUID = -6006423682361564704L;

    public MailCompositionAttachmentException( String msg )
    {
        super( msg );
    }

    public MailCompositionAttachmentException( String msg, Throwable ex )
    {
        super( msg, ex );
    }
}
