/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
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
