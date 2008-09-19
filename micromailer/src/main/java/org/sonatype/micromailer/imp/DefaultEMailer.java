package org.sonatype.micromailer.imp;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.micromailer.EMailer;
import org.sonatype.micromailer.EmailerConfiguration;
import org.sonatype.micromailer.MailComposer;
import org.sonatype.micromailer.MailCompositionException;
import org.sonatype.micromailer.MailCompositionTemplateException;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailRequestSource;
import org.sonatype.micromailer.MailRequestStatus;
import org.sonatype.micromailer.MailSender;
import org.sonatype.micromailer.MailStorage;
import org.sonatype.micromailer.MailType;
import org.sonatype.micromailer.MailTypeSource;

/**
 * The default implementation of EMailer component.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultEMailer
    extends AbstractLogEnabled
    implements EMailer
{
    /**
     * @plexus.requirement
     */
    private MailTypeSource mailTypeSource;

    /**
     * @plexus.requirement
     */
    private MailComposer mailComposer;

    /**
     * @plexus.requirement
     */
    private MailStorage mailStorage;

    /**
     * @plexus.requirement
     */
    private MailSender mailSender;

    private EmailerConfiguration emailerConfiguration = new EmailerConfiguration();
    
    private ExecutorService executor = Executors.newCachedThreadPool();

    // =========================================================================
    // EMailer iface

    public void configure( EmailerConfiguration config )
    {
        this.emailerConfiguration = config;
    }

    public MailTypeSource getMailTypeSource()
    {
        return mailTypeSource;
    }

    public MailComposer getMailComposer()
    {
        return mailComposer;
    }

    public MailStorage getMailStorage()
    {
        return mailStorage;
    }

    public MailSender getMailSender()
    {
        return mailSender;
    }

    public MailRequestStatus sendMail( MailRequest request )
    {
        return handleMailRequest( request );
    }

    public void sendMailBatch( MailRequestSource mailRequestSource )
    {
        if ( mailRequestSource.hasWaitingRequests() )
        {
            getLogger().info( "* Got batch request, processing it..." );

            for ( Iterator<MailRequest> i = mailRequestSource.getRequestIterator(); i.hasNext(); )
            {
                MailRequest request = i.next();

                MailRequestStatus status = handleMailRequest( request );

                mailRequestSource.setMailRequestStatus( request, status );
            }

            getLogger().info( "* Finished batch request processing." );
        }
    }

    // =========================================================================
    // Internal stuff

    protected MailRequestStatus handleMailRequest( MailRequest request )
    {
        getLogger().info( "  Handling mail request " + request.getRequestId() );

        MailRequestStatus status = new MailRequestStatus( request );

        executor.execute(
             new RunnableMailer( getLogger(), request, mailTypeSource,
                                 mailComposer, emailerConfiguration,
                                 mailStorage, mailSender, status ) );

        return status;
    }
    
    private static final class RunnableMailer
        implements Runnable
    {
        private Logger logger;
        private MailRequest request;
        private MailTypeSource mailTypeSource;
        private MailComposer mailComposer;
        private EmailerConfiguration emailerConfiguration;
        private MailStorage mailStorage;
        private MailSender mailSender;
        private MailRequestStatus status;
        
        protected RunnableMailer( Logger logger, MailRequest request, MailTypeSource mailTypeSource,
                                  MailComposer mailComposer, EmailerConfiguration emailerConfiguration,
                                  MailStorage mailStorage, MailSender mailSender, MailRequestStatus status )
        {
            this.logger = logger;
            this.request = request;
            this.mailTypeSource = mailTypeSource;
            this.mailComposer = mailComposer;
            this.emailerConfiguration = emailerConfiguration;
            this.mailStorage = mailStorage;
            this.mailSender = mailSender;
            this.status = status;
        }
        public void run()
        {
            try
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "  PREPARING " + request.getRequestId() );
                }

                MailType mailType = mailTypeSource.getMailType( request.getMailTypeId() );

                if ( mailType != null )
                {
                    // prepare it if needed
                    mailComposer.composeMail( emailerConfiguration, request, mailType );
                    status.setPrepared( true );

                    // store it if needed
                    if ( request.isStoreable() || mailType.isStoreable() )
                    {
                        mailStorage.saveMailRequest( request );
                        status.setStored( true );
                    }

                    // send it
                    mailSender.sendMail( emailerConfiguration, request, mailType );
                    status.setSent( true );
                }
                else
                {
                    status.setErrorCause( new MailCompositionTemplateException( "Unknown mailType with ID='"
                        + request.getMailTypeId() + "'" ) );
                }
            }
            catch ( MailCompositionException ex )
            {
                status.setErrorCause( ex );
            }
            catch ( IOException ex )
            {
                logger.warn( "IOException during handling of mail request Id = [" + request.getRequestId() + "]", ex );

                status.setErrorCause( ex );
            }   
        }
    }
}
