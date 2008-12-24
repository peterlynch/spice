package org.codehaus.plexus.swizzle.jira.authentication;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public interface AuthenticationSource
{
    String getLogin();

    String getPassword();
}
