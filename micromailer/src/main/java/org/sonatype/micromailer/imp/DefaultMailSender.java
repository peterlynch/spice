package org.sonatype.micromailer.imp;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.micromailer.Address;
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
    public static final String MESSAGE_FROM = "From";

    public static final String MESSAGE_TO = "To";

    public static final String MESSAGE_BCC = "Bcc";

    public static final String MESSAGE_SENDER = "Sender";

    public static final String X_MESSAGE_ID_HEADER = "X-EMailer-Mail-Request-ID";

    // ==

    private static final String MAIL_HOST = "mail.smtp.host";

    private static final String MAIL_PORT = "mail.smtp.port";

    private static final String MAIL_SMTP_FROM = "mail.smtp.from";

    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";

    private static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

    private static final String MAIL_TRANSPORT_TLS = "mail.smtp.starttls.enable";

    private static final String MAIL_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";

    private static final String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";

    private static final String MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";

    private static final String MAIL_DEBUG = "mail.debug";

    public void sendMail( EmailerConfiguration configuration, MailRequest request, MailType mailType )
        throws MailCompositionAttachmentException,
            MailCompositionMessagingException
    {
        try
        {
            Session session = getSession( configuration );

            MimeMessage message = new MimeMessage( session );

            MimeMultipart root = new MimeMultipart( "related" );

            message.setContent( root );

            if ( request.getCustomHeaders().size() > 0 )
            {
                for ( String key : request.getCustomHeaders().keySet() )
                {
                    message.addHeader( key, request.getCustomHeaders().get( key ) );
                }
            }

            if ( request.getRequestId() != null )
            {
                message.addHeader( X_MESSAGE_ID_HEADER, request.getRequestId() );
            }

            if ( request.getSender() != null )
            {
                message.setSender( request.getSender().getInternetAddress( request.getEncoding() ) );
            }

            if ( request.getFrom() != null )
            {
                message.setFrom( request.getFrom().getInternetAddress( request.getEncoding() ) );
            }

            if ( request.getReplyTo() != null )
            {
                message.setReplyTo( new InternetAddress[] { request.getReplyTo().getInternetAddress(
                    request.getEncoding() ) } );
            }

            if ( request.getSentDate() != null )
            {
                message.setSentDate( request.getSentDate() );
            }
            else
            {
                message.setSentDate( new Date() );
            }

            setRecipientsFromList( request.getEncoding(), message, RecipientType.TO, request.getToAddresses() );

            setRecipientsFromList( request.getEncoding(), message, RecipientType.BCC, request.getBccAddresses() );

            setRecipientsFromList( request.getEncoding(), message, RecipientType.CC, request.getCcAddresses() );

            // add content and any inline resource we have

            message.setSubject( request.getExpandedSubject(), request.getEncoding() );

            MimeBodyPart body = new MimeBodyPart();

            root.addBodyPart( body );

            if ( mailType.isBodyIsHtml() )
            {
                body.setContent( request.getExpandedBody(), "text/html;charset=" + request.getEncoding() );
            }
            else
            {
                body.setText( request.getExpandedBody(), request.getEncoding() );
            }

            for ( String key : mailType.getInlineResources().keySet() )
            {
                MimeBodyPart mimeBodyPart = new MimeBodyPart();

                mimeBodyPart.setDisposition( MimeBodyPart.INLINE );

                mimeBodyPart.setContentID( key );

                mimeBodyPart.setDataHandler( new DataHandler( mailType.getInlineResources().get( key ) ) );

                root.addBodyPart( mimeBodyPart );
            }

            // add attachemtns if any

            for ( String key : request.getAttachmentMap().keySet() )
            {
                MimeBodyPart mimeBodyPart = new MimeBodyPart();

                mimeBodyPart.setDisposition( MimeBodyPart.ATTACHMENT );

                mimeBodyPart.setFileName( key );

                mimeBodyPart.setDataHandler( new DataHandler( request.getAttachmentMap().get( key ) ) );

                root.addBodyPart( mimeBodyPart );
            }

            // validate some of it

            if ( message.getHeader( MESSAGE_FROM, null ) == null )
            {
                // RFC822: From is MANDATORY
                // http://www.ietf.org/rfc/rfc822.txt
                throw new IllegalArgumentException( "E-Mail 'From' field is mandatory!" );
            }
            if ( message.getHeader( MESSAGE_TO, null ) == null && message.getHeader( MESSAGE_BCC, null ) == null )
            {
                // RFC822: Bcc OR To is MANDATORY
                // http://www.ietf.org/rfc/rfc822.txt
                throw new IllegalArgumentException( "One of the 'To' or 'Bcc' header is mandatory!" );
            }

            message.saveChanges();

            // send it
            Transport.send( message );
        }
        catch ( UnsupportedEncodingException ex )
        {
            throw new MailCompositionMessagingException( "Unsupported encoding occured!", ex );
        }
        catch ( IllegalArgumentException ex )
        {
            throw new MailCompositionMessagingException( "IllegalArgument occured!", ex );
        }
        catch ( MessagingException ex )
        {
            throw new MailCompositionMessagingException( "MessagingException occured!", ex );
        }
    }

    protected void setRecipientsFromList( String encoding, MimeMessage message, RecipientType type,
        List<Address> addresses )
        throws MessagingException,
            UnsupportedEncodingException
    {
        if ( addresses == null || addresses.size() == 0 )
        {
            return;
        }

        InternetAddress[] adrs = new InternetAddress[addresses.size()];

        for ( int i = 0; i < addresses.size(); i++ )
        {
            adrs[i] = addresses.get( i ).getInternetAddress( encoding );
        }

        message.setRecipients( type, adrs );
    }

    public Session getSession( EmailerConfiguration config )
        throws MailCompositionMessagingException
    {
        Properties properties = new Properties( System.getProperties() );

        properties.setProperty( MAIL_TRANSPORT_PROTOCOL, "smtp" );

        if ( StringUtils.isEmpty( config.getMailHost() ) )
        {
            config.setMailHost( properties.getProperty( MAIL_HOST ) );
        }

        if ( StringUtils.isEmpty( config.getMailHost() ) )
        {
            throw new MailCompositionMessagingException( "Cannot find valid hostname for mail session" );
        }

        properties.setProperty( MAIL_PORT, String.valueOf( config.getMailPort() ) );
        properties.setProperty( MAIL_HOST, config.getMailHost() );
        properties.setProperty( MAIL_DEBUG, String.valueOf( config.isDebug() ) );

        Authenticator authenticator = config.getAuthenticator();

        if ( authenticator != null )
        {
            properties.setProperty( MAIL_TRANSPORT_TLS, config.isTls() ? "true" : "false" );
            properties.setProperty( MAIL_SMTP_AUTH, "true" );
        }

        if ( config.isSsl() )
        {
            properties.setProperty( MAIL_SMTP_SOCKET_FACTORY_PORT, String.valueOf( config.getMailPort() ) );
            properties.setProperty( MAIL_SMTP_SOCKET_FACTORY_CLASS, "javax.net.ssl.SSLSocketFactory" );
            properties.setProperty( MAIL_SMTP_SOCKET_FACTORY_FALLBACK, "false" );
        }

        if ( config.getBounceAddress() != null )
        {
            properties.setProperty( MAIL_SMTP_FROM, config.getBounceAddress() );
        }

        return Session.getInstance( properties, authenticator );
    }

}
