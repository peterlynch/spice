/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.micromailer;

import javax.mail.internet.MimeMessage;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.micromailer.imp.DefaultMailType;
import org.sonatype.micromailer.imp.HtmlMailType;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

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
        throws Exception
    {
        MailRequest request = new MailRequest( "testId", DefaultMailType.DEFAULT_TYPE_ID );

        MailRequestStatus status = eMailer.sendMail( request );

        int count = 0;

        while ( !status.isSent() && count < 10 )
        {
            Thread.sleep( 100 );
            count++;
        }

        assertFalse( status.isSent() );

        assertNotNull( status.getErrorCause() );

        assertEquals( MailCompositionMessagingException.class, status.getErrorCause().getClass() );
    }

    public void testRealOnLocalhost()
        throws Exception
    {
        final String host = "localhost";
        final int port = 42358;
        final String username = "smtp-username";
        final String password = "smtp-password";
        final String systemMailAddress = "system@nexus.org";

        // mail server config
        ServerSetup smtp = new ServerSetup( port, null, ServerSetup.PROTOCOL_SMTP );
        GreenMail server = new GreenMail( smtp );
        server.setUser( systemMailAddress, username, password );
        server.start();

        // mailer config
        EmailerConfiguration config = new EmailerConfiguration();
        config.setMailHost( host );
        config.setMailPort( port );
        config.setUsername( username );
        config.setPassword( password );
        config.setSsl( false );
        config.setTls( false );
        config.setDebug( true );

        eMailer.configure( config );

        // prepare a mail request
        MailRequest request = new MailRequest( "Mail-Test", HtmlMailType.HTML_TYPE_ID );
        request.setFrom( new Address( systemMailAddress, "Nexus Manager" ) );
        request.getToAddresses().add( new Address( "user1@nexus.org" ) );
        request.getToAddresses().add( new Address( "user2@nexus.org" ) );
        request.getToAddresses().add( new Address( "user3@nexus.org" ) );
        request.getToAddresses().add( new Address( "user4@nexus.org" ) );
        request.getBodyContext().put( DefaultMailType.SUBJECT_KEY, "Nexus: Mail Test Begin." );
        StringBuilder body = new StringBuilder();
        body.append( "The following artifacts have been staged to the test Repository:<br><br>" );
        body
            .append( "<i>(Set the Base URL parameter in Nexus Server Administration to retrieve links to these artifacts in future emails)</i><br><br>" );
        body.append( "<a href='http://www.sonatype.com'>Sonatype</a><br>" );
        request.getBodyContext().put( DefaultMailType.BODY_KEY, body.toString() );

        // send the mail
        eMailer.sendMail( request );

        // validate
        long timeout = 1000;
        if ( !server.waitForIncomingEmail( timeout, 1 ) )
        {
            fail( "Could not receive any email in a timeout of " + timeout );
        }
        MimeMessage msgs[] = server.getReceivedMessages();
        assertEquals( 4, msgs.length );
        String receivedBody = GreenMailUtil.getBody( msgs[0] );
        assertTrue( receivedBody.contains( "Sonatype" ) );

    }

    public void testEmptyUsername()
    {
        final String host = "localhost";
        final int port = 12345;

        // mailer config
        EmailerConfiguration config = new EmailerConfiguration();
        config.setMailHost( host );
        config.setMailPort( port );
        config.setUsername( null );
        config.setPassword( null );
        config.setSsl( false );
        config.setTls( false );
        config.setDebug( true );

        // should be null
        Assert.assertNull( config.getAuthenticator() );

        config.setUsername( "" );
        config.setPassword( null );
        // should be null
        Assert.assertNull( config.getAuthenticator() );

        config.setUsername( null );
        config.setPassword( "" );
        // should be null
        Assert.assertNull( config.getAuthenticator() );
        
        config.setUsername( "user" );
        config.setPassword( "" );
        // should be null
        Assert.assertNotNull( config.getAuthenticator() );
        
        config.setUsername( "" );
        config.setPassword( "invalid" );
        // should be null
        Assert.assertNull( config.getAuthenticator() );
        
    }

}
