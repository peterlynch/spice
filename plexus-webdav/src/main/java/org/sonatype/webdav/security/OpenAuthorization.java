/*
 * 
 */
package org.sonatype.webdav.security;

// TODO: Auto-generated Javadoc
/**
 * Class that always authorizes. Used for Nexus, since auth/authz are handled there.
 * 
 * @author cstamas
 * @plexus.component role-hint="open"
 */
public class OpenAuthorization
    implements Authorization
{

    /* (non-Javadoc)
     * @see org.sonatype.webdav.security.Authorization#authorize(org.sonatype.webdav.security.User, org.sonatype.webdav.security.Permission)
     */
    public boolean authorize( User user, Permission permission )
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.sonatype.webdav.security.Authorization#authorize(org.sonatype.webdav.security.User, org.sonatype.webdav.security.Permission, java.lang.String)
     */
    public boolean authorize( User user, Permission permission, String path )
    {
        return true;
    }

}
