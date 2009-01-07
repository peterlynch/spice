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
package org.sonatype.micromailer.imp;

import java.io.IOException;

import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailStorage;

/**
 * A "default" (a doing nothing) MailStorage implementation.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultMailStorage
    implements MailStorage
{
    public MailRequest loadMailRequest( String requestId )
        throws IOException
    {
        throw new UnsupportedOperationException( "NullMailStorage is unable to load mail!" );
    }

    public void saveMailRequest( MailRequest request )
        throws IOException
    {
        // silently do a big nothing
    }
}
