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
package org.sonatype.appbooter.ctl;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Utilities for network stuff.
 */
public final class ControllerUtil
{
    private ControllerUtil()
    {
    }

    public static void close( Closeable closeable )
    {
        if ( closeable != null )
        {
            try
            {
                closeable.close();
            }
            catch ( IOException closeError )
            {
            }
        }
    }

    public static void close( ServerSocket socket )
    {
        if ( socket != null )
        {
            try
            {
                socket.close();
            }
            catch ( IOException closeError )
            {
            }
        }
    }

    public static void close( Socket socket )
    {
        if ( socket != null )
        {
            try
            {
                socket.close();
            }
            catch ( IOException closeError )
            {
            }
        }
    }
}
