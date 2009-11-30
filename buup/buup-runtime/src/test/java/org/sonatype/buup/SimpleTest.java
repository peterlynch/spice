package org.sonatype.buup;

import java.net.URL;

import junit.framework.TestCase;

public class SimpleTest extends TestCase
{
    public void testSimple() throws Exception
    {
        URL url = new URL("nexus://groups/aGroup");
        
        assertEquals( "nexus", url.getProtocol() );
    }

}
