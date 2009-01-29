package org.sonatype.jettytestsuite.proxy;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public final class StatusServlet
    extends GenericServlet
{

    private static final long serialVersionUID = 7184508159372212827L;

    private final int returnCode;

    public StatusServlet( int returnCode )
    {
        this.returnCode = returnCode;
    }

    @Override
    public void service( ServletRequest req, ServletResponse response )
        throws ServletException, IOException
    {
        HttpServletResponse res = (HttpServletResponse) response;
        res.sendError( returnCode );
    }
}
