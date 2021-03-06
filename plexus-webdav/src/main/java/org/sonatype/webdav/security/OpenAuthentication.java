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
