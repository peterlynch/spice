package org.sonatype.micromailer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import junit.framework.TestCase;

public class AddressTest
    extends TestCase
{
    public void testGood()
        throws Exception
    {
        Address adr = new Address( "a@b.c" );

        InternetAddress iadr = adr.getInternetAddress( "UTF-8" );

        assertEquals( "a@b.c", iadr.getAddress() );

        assertNull( "Should be null", iadr.getPersonal() );

        adr = new Address( "a@b.c", "Kaizer Soze" );

        iadr = adr.getInternetAddress( "UTF-8" );

        assertEquals( "a@b.c", iadr.getAddress() );

        assertEquals( "Kaizer Soze", iadr.getPersonal() );
    }

    public void testBad()
    {
        Address adr = new Address( "abc" );

        try
        {
            adr.getInternetAddress( "UTF-8" );
            
            fail("Bad email address, should fail!");
        }
        catch ( Exception e )
        {
            // good
            assertEquals( AddressException.class, e.getClass() );
        }
    }
}
