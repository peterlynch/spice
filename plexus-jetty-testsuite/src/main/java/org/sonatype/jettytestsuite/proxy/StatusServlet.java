package org.sonatype.jettytestsuite.proxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class StatusServlet
    extends AbstractMonitorServlet
{

    private static final long serialVersionUID = 7184508159372212827L;

    private final int returnCode;

    public StatusServlet( int returnCode )
    {
        this.returnCode = returnCode;
    }

    @Override
    public void service( HttpServletRequest req, HttpServletResponse res )
        throws ServletException, IOException
    {
        res.sendError( returnCode );
    }
}
