/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package net.java.dev.openim.tools;

import java.io.InputStream;
import java.io.IOException;

import org.codehaus.plexus.logging.Logger;

/**
 * @version 1.5
 * @author AlAg
 */
public class InputStreamDebugger
    extends InputStream
{

    private InputStream is;
    private Logger logger;
    private long id;

    public InputStreamDebugger( InputStream is, Logger logger, long id )
    {
        this.is = is;
        this.logger = logger;
        this.id = id;
    }

    public int available()
        throws IOException
    {
        return is.available();
    }

    public void close()
        throws IOException
    {
        is.close();
    }

    public void mark( int readlimit )
    {
        is.mark( readlimit );
    }

    public boolean markSupported()
    {
        return is.markSupported();
    }

    public int read()
        throws IOException
    {
        int b = is.read();
        logger.info( "Input (" + id + "): " + new Character( (char) b ) );
        return b;
    }

    public int read( byte[] b )
        throws IOException
    {
        int i = is.read( b );
        if ( i != -1 )
        {
            logger.info( "Input (" + id + "): " + new String( b, 0, i ) );
        }
        return i;
    }

    public int read( byte[] b, int off, int len )
        throws IOException
    {
        int i = is.read( b, off, len );
        if ( i != -1 )
        {
            logger.info( "Input (" + id + "): " + new String( b, off, i ) );
        }

        return i;
    }

    public void reset()
        throws IOException
    {
        is.reset();
    }

    public long skip( long n )
        throws IOException
    {
        return is.skip( n );
    }
}
