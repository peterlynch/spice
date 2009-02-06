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
package org.sonatype.webdav;

import org.sonatype.webdav.util.RequestUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;


/**
 * @author Jason van Zyl
 * @author Andrew Williams
 */
public abstract class AbstractResource
    implements Resource
{
    private String mime = null;

    public static void copy( final InputStream input, final OutputStream output, final int bufferSize )
        throws IOException
    {
        try
        {
            if ( input == null )
            {
                return;
            }
    
            final byte[] buffer = new byte[bufferSize];
    
            int n;
    
            while ( -1 != ( n = input.read( buffer ) ) )
            {
                output.write( buffer, 0, n );
            }
    
            output.flush();

        }
        finally
        {
            if( input != null )
                try { input.close(); } catch(Exception e ) {}

            if( output != null )
                try { output.close(); } catch(Exception e ) {}
        }
    }

    public String getETag( boolean b )
    {
        return null;
    }

    public String getETag()
    {
        return null;
    }

    public String getLastModifiedHttp()
    {
        return RequestUtil.formatHttpDate( new Date( getLastModified() ) );
    }

    public String getMimeType()
    {
        return mime;
    }

    public void setMimeType( String type )
    {
        this.mime = type;
    }
}
