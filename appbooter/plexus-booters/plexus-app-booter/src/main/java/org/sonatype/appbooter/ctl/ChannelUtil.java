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

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.Selector;

public final class ChannelUtil
{

    private ChannelUtil(){}

    public static void close( Channel channel )
    {
        if ( channel != null && channel.isOpen() )
        {
            try
            {
                channel.close();
            }
            catch ( IOException closeError )
            {
            }
        }
    }

    public static void close( Selector selector )
    {
        if ( selector != null && selector.isOpen() )
        {
            try
            {
                selector.close();
            }
            catch ( IOException e )
            {
            }
        }
    }

}
