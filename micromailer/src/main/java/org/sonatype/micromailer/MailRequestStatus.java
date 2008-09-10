package org.sonatype.micromailer;

/**
 * Represents the status of the executed mail request.
 * 
 * @author cstamas
 */
public class MailRequestStatus
{
    /** The mail request. */
    private final MailRequest mailRequest;

    /** Is the mail prepared for sending? */
    private boolean prepared;

    /** Is mail persistently stored? */
    private boolean stored;

    /** Is mail sent? */
    private boolean sent;

    /** Any problem during processing phases */
    private Throwable errorCause;

    public MailRequestStatus( MailRequest request )
    {
        super();

        this.mailRequest = request;

        this.prepared = false;

        this.stored = false;

        this.sent = false;

        this.errorCause = null;
    }

    public MailRequest getMailRequest()
    {
        return mailRequest;
    }

    public boolean isPrepared()
    {
        return prepared;
    }

    public void setPrepared( boolean prepared )
    {
        this.prepared = prepared;
    }

    public boolean isStored()
    {
        return stored;
    }

    public void setStored( boolean stored )
    {
        this.stored = stored;
    }

    public boolean isSent()
    {
        return sent;
    }

    public void setSent( boolean sent )
    {
        this.sent = sent;
    }

    public Throwable getErrorCause()
    {
        return errorCause;
    }

    public void setErrorCause( Throwable ex )
    {
        this.errorCause = ex;
    }

}
