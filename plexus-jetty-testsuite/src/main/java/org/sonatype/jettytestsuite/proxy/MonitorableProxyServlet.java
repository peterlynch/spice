package org.sonatype.jettytestsuite.proxy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.proxy.AsyncProxyServlet;

/**
 *
 */

public class MonitorableProxyServlet
    extends AsyncProxyServlet
{

    public List<String> getAccessedUris()
    {
        if ( accessedUris == null )
        {
            accessedUris = new ArrayList<String>();
        }
        return accessedUris;
    }

    public void setAccessedUris( List<String> accessedUris )
    {
        this.accessedUris = accessedUris;
    }

    private List<String> accessedUris;

    @Override
    public void service( ServletRequest req, ServletResponse res )
        throws ServletException, IOException
    {
        String uri = ( (Request) req ).getUri().toString();
        getAccessedUris().add( uri );
        super.service( req, res );
    }

}