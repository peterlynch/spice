package org.sonatype.micromailer;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.micromailer.imp.DefaultMailType;

public class EMailerTest
    extends PlexusTestCase
{
    private EMailer eMailer;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        eMailer = (EMailer) lookup( EMailer.class );
    }

    public void testWithoutConfiguration()
    {
        MailRequest request = new MailRequest( "testId", DefaultMailType.DEFAULT_TYPE_ID );

        MailRequestStatus status = eMailer.sendMail( request );

        assertFalse( status.isSent() );

        assertNotNull( status.getErrorCause() );

        assertEquals( MailCompositionMessagingException.class, status.getErrorCause().getClass() );
    }

    /**
     * Turned off since it needs localhost SMTP server
     */
    public void OFFEDtestWithLocalhost()
    {
        EmailerConfiguration config = new EmailerConfiguration();

        config.setMailHost( "is-micro.myip.hu" );

        config.setTls( true );
        
        config.setUsername( "XXX" );

        config.setPassword( "XXX" );

        config.setDebug( true );

        eMailer.configure( config );

        MailRequest request = new MailRequest( "testId", DefaultMailType.DEFAULT_TYPE_ID );

        request.setFrom( new Address( "test@sonatype.com" ) );

        request.getToAddresses().add( new Address( "t.cservenak@gmail.com", "TCs" ) );

        request.getBodyContext().put( DefaultMailType.SUBJECT_KEY, "Test Subject" );

        request.getBodyContext().put( DefaultMailType.BODY_KEY, "Some mail body" );

        MailRequestStatus status = eMailer.sendMail( request );

        if ( status.getErrorCause() != null )
        {
            status.getErrorCause().printStackTrace();
        }

        assertTrue( status.isSent() );
    }

}
