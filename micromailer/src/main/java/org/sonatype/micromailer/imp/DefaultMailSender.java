package org.sonatype.micromailer.imp;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.sonatype.micromailer.EmailerConfiguration;
import org.sonatype.micromailer.MailCompositionAttachmentException;
import org.sonatype.micromailer.MailCompositionMessagingException;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailSender;
import org.sonatype.micromailer.MailType;

/**
 * The JavaMail sender.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultMailSender
    implements MailSender
{
    public void sendMail( EmailerConfiguration configuration, MailRequest request, MailType mailType )
        throws MailCompositionAttachmentException,
            MailCompositionMessagingException
    {
        try
        {
            Session session = configuration.getSession();

            MimeMessage message = request.getMimeMessage();

            message.saveChanges();

            // send it
            Transport t = session.getTransport();

            try
            {
                t.connect();

                t.sendMessage( message, message.getAllRecipients() );
            }
            finally
            {
                t.close();
            }
        }
        catch ( MessagingException ex )
        {
            throw new MailCompositionMessagingException( "MessagingException occured!", ex );
        }
    }
}
