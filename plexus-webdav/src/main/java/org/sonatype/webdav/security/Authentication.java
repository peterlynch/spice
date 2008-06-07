package org.sonatype.webdav.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface Authentication
{
    User authenticate( HttpServletRequest req, HttpServletResponse res, HttpSession session );

    void challenge( User user, HttpServletRequest req, HttpServletResponse res, HttpSession session )
        throws IOException;
}
