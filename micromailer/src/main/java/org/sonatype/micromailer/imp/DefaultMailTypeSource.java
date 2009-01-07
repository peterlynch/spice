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
package org.sonatype.micromailer.imp;

import java.util.Collection;
import java.util.Map;

import org.sonatype.micromailer.MailType;
import org.sonatype.micromailer.MailTypeSource;

/**
 * A mail type source.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultMailTypeSource
    implements MailTypeSource
{
    /**
     * @plexus.requirement role="org.sonatype.micromailer.MailType"
     */
    private Map<String, MailType> mailTypes;

    public Collection<MailType> getKnownMailTypes()
    {
        return mailTypes.values();
    }

    public MailType getMailType( String id )
    {
        if ( mailTypes.containsKey( id ) )
        {
            return mailTypes.get( id );
        }
        else
        {
            return null;
        }
    }
}
