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
