package org.sonatype.micromailer;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.codehaus.plexus.util.IOUtil;

public class ClasspathResourceTest
    extends TestCase
{

    public void testSimple()
    {
        ClasspathResource res = new ClasspathResource( "/sample.txt", "sample", "text/plain" );

        assertEquals( "text/plain", res.getContentType() );

        assertEquals( "sample", res.getName() );

        assertEquals( "/sample.txt", res.getPath() );

        String cnt = null;

        try
        {
            InputStream is = res.getInputStream();

            cnt = IOUtil.toString( is );

            assertEquals( "SAMPLE", cnt );
        }
        catch ( IOException e )
        {
            fail( "Should have succeed!" );
        }

        try
        {
            res.getOutputStream();

            fail( "Classpath resource should not allow this!" );
        }
        catch ( UnsupportedOperationException e )
        {
            // good
        }
        catch ( IOException e )
        {
            fail( "Should have succeed!" );
        }
    }
}
