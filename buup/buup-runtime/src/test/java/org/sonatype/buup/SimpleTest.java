package org.sonatype.buup;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

public class SimpleTest
    extends TestCase
{
    public void testSimple()
        throws Exception
    {
        URL url = new URL( "nexus://groups/aGroup" );

        assertEquals( "nexus", url.getProtocol() );
    }

    public void testFile()
    {
        File base = new File( "/Users/cstamas" );

        String child = null;

        File result;

        child = "boo";

        result = new File( base, child );

        assertEquals( "/Users/cstamas/boo", result.getAbsolutePath() );

        child = "/boo";

        result = new File( base, child );

        assertEquals( "/boo", result.getAbsolutePath() );
    }

}
