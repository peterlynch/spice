package org.sonatype.micromailer;

/**
 * The main component responsible for sending mails, either one-by-one or in batch mode.
 * 
 * @author cstamas
 */
public interface EMailer
{
    // configuration

    void configure( EmailerConfiguration config );

    // exposing components

    MailTypeSource getMailTypeSource();

    MailComposer getMailComposer();

    MailStorage getMailStorage();

    MailSender getMailSender();

    // mail sending
    MailRequestStatus sendMail( MailRequest request );

    void sendMailBatch( MailRequestSource mailRequestSource );
}
