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
