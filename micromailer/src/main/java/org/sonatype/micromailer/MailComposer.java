package org.sonatype.micromailer;

/**
 * MailComposer component actually composes the MIME mail message. The resulting MIME message could be sent or saved.
 * 
 * @author cstamas
 */
public interface MailComposer
{
    void composeMail( EmailerConfiguration configuration, MailRequest request, MailType mailType )
        throws MailCompositionTemplateException,
            MailCompositionAttachmentException,
            MailCompositionMessagingException;
}
