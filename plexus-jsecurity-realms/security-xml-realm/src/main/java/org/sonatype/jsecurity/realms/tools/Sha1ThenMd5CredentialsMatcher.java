package org.sonatype.jsecurity.realms.tools;

import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.credential.CredentialsMatcher;
import org.jsecurity.authc.credential.Md5CredentialsMatcher;
import org.jsecurity.authc.credential.Sha1CredentialsMatcher;

/**
 * For users migrated from Artifactory, their password is encrypted with md5, while nexus users' password is enrypted
 * with sha1, so here we use first try sha1, then md5, to meet both requirements.
 * 
 * @author Juven Xu
 */
public class Sha1ThenMd5CredentialsMatcher
    implements CredentialsMatcher
{
    private CredentialsMatcher sha1Matcher = new Sha1CredentialsMatcher();

    private CredentialsMatcher md5Matcher = new Md5CredentialsMatcher();

    public boolean doCredentialsMatch( AuthenticationToken token, AuthenticationInfo info )
    {
        if ( sha1Matcher.doCredentialsMatch( token, info ) )
        {
            return true;
        }

        if ( md5Matcher.doCredentialsMatch( token, info ) )
        {
            return true;
        }

        return false;
    }
}
