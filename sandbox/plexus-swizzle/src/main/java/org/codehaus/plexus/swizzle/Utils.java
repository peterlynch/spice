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
package org.codehaus.plexus.swizzle;

import java.io.InputStream;
import java.io.IOException;

/**
 * @author John Tolentino
 */
public class Utils
{

    public static String streamToString( InputStream in )
        throws IOException
        {
        StringBuffer text = new StringBuffer();
        try
        {
            int b;
            while ( ( b = in.read() ) != -1 )
            {
                text.append( (char) b );
            }
        }
        finally
        {
            in.close();
        }
        return text.toString();
    }

}
