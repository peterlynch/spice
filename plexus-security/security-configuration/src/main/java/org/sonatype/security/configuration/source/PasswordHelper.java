/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.configuration.source;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

/**
 * FIXME This needs to be abstracted, as this is just a copy of the class in nexus. The problem is if we move this to
 * base-configuration (or something) it becomes less secure, as we are using the same key for everything)
 */
@Component( role = PasswordHelper.class )
public class PasswordHelper
{

    private static final String ENC = "CMMDwoV";

    @Requirement
    private PlexusCipher plexusCipher;

    public String encrypt( String password )
        throws PlexusCipherException
    {
        return encrypt( password, ENC );
    }

    public String encrypt( String password, String encoding )
        throws PlexusCipherException
    {
        if ( password != null )
        {
            return plexusCipher.encryptAndDecorate( password, encoding );
        }

        return null;
    }

    public String decrypt( String encodedPassword )
        throws PlexusCipherException
    {
        return decrypt( encodedPassword, ENC );
    }

    public String decrypt( String encodedPassword, String encoding )
        throws PlexusCipherException
    {
        // check if the password is encrypted
        if ( !plexusCipher.isEncryptedString( encodedPassword ) )
        {
            return encodedPassword;
        }

        if ( encodedPassword != null )
        {
            return plexusCipher.decryptDecorated( encodedPassword, encoding );
        }
        return null;
    }
}
