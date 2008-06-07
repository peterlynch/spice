/*
 * 
 */
package org.sonatype.webdav.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * Dummy authentication.
 * 
 * @author cstamas
 * @plexus.component role-hint="open"
 */
public class OpenAuthentication
    implements Authentication
{

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.webdav.security.Authentication#authenticate(java.lang.String, java.lang.String,
     *      java.lang.Object)
     */
    public User authenticate( String username, String password, Object session )
    {
        return SimpleUser.ANONYMOUS_USER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.webdav.security.Authentication#authenticate(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpSession)
     */
    public User authenticate( HttpServletRequest req, HttpServletResponse res, HttpSession session )
    {
        return SimpleUser.ANONYMOUS_USER;
    }

    public void challenge(  User user, HttpServletRequest req, HttpServletResponse res, HttpSession session  )
        throws IOException
    {
        res.sendError( HttpServletResponse.SC_FORBIDDEN );
    }

}
