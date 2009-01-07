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
package org.sonatype.webdav;

import org.sonatype.webdav.security.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class DefaultMethodExecutionContext
    implements MethodExecutionContext
{

    private User user;

    private HttpServletRequest httpServletRequest;

    private HttpServletResponse httpServletResponse;

    public DefaultMethodExecutionContext( User user, HttpServletRequest req, HttpServletResponse res )
    {
        super();
        this.user = user;
        this.httpServletRequest = req;
        this.httpServletResponse = res;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser( User user )
    {
        this.user = user;
    }

    public HttpServletRequest getHttpServletRequest()
    {
        return httpServletRequest;
    }

    public void setHttpServletRequest( HttpServletRequest httpServletRequest )
    {
        this.httpServletRequest = httpServletRequest;
    }

    public HttpServletResponse getHttpServletResponse()
    {
        return httpServletResponse;
    }

    public void setHttpServletResponse( HttpServletResponse httpServletResponse )
    {
        this.httpServletResponse = httpServletResponse;
    }

}
