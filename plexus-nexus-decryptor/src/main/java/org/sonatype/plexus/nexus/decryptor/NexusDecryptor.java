/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.sonatype.plexus.nexus.decryptor;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.sec.dispatcher.PasswordDecryptor;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
@Component (role=PasswordDecryptor.class, hint="nexus")
public class NexusDecryptor
    implements PasswordDecryptor
{
    
    @Requirement
    PlexusCipher _cipher;
    
    public String decrypt( String pass, Map attributes, Map config )
    throws SecDispatcherException
    {
        if( pass == null )
            return null;
        
        return PlexusCipher.ENCRYPTED_STRING_DECORATION_START + pass + PlexusCipher.ENCRYPTED_STRING_DECORATION_STOP; 
    }
}
