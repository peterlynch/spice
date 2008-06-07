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
