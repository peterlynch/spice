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

    /**
     * Turned off since it needs localhost SMTP server
     */
    public void OFFEDtestWithLocalhost()
        throws Exception
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
        
        int count = 0;
        
        while ( !status.isSent() && count < 10 )
        {
            Thread.sleep( 100 );
            count++;
        }

        if ( status.getErrorCause() != null )
        {
            status.getErrorCause().printStackTrace();
        }
        
        assertTrue( status.isSent() );
    }

}
