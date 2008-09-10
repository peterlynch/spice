package org.sonatype.micromailer;

public interface MailSender
{
    void sendMail( EmailerConfiguration configuration, MailRequest request, MailType mailType )
        throws MailCompositionAttachmentException,
            MailCompositionMessagingException;
}
