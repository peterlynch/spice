 /**
  * Copyright (C) 2008 Sonatype Inc.
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package org.sonatype.appbooter.ctl;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class ControllerUtil
{

    private ControllerUtil(){}

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
