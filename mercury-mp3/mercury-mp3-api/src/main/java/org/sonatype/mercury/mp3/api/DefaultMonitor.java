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

package org.sonatype.mercury.mp3.api;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

/**
 * @author Oleg Gusakov
 * @version $Id$
 */
public class DefaultMonitor
    implements Monitor
{
    Writer _writer;
    boolean _timestamp = true;
    

    public DefaultMonitor( boolean timestamp )
    {
        this( System.out );
        this._timestamp = timestamp;
    }

    public DefaultMonitor()
    {
        this( System.out );
    }

    public DefaultMonitor( OutputStream os )
    {
        _writer = new OutputStreamWriter( os );
    }

    public DefaultMonitor( Writer writer )
    {
        _writer = writer;
    }

    public void message( String msg )
    {
        try
        {
            if ( _writer != null )
            {
                if( _timestamp )
                {
                    _writer.write( new Date().toString() );
                    _writer.write( ": " );
                }
                _writer.write( msg );
                _writer.write( "\n" );
                _writer.flush();
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

}
