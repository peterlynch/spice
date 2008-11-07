package org.sonatype.jettytestsuite.proxy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.mortbay.jetty.HttpURI;
import org.mortbay.jetty.Request;
import org.mortbay.proxy.AsyncProxyServlet;

/**
 *
 */

public class MonitorableProxyServlet
    extends AsyncProxyServlet
{

    public List<HttpURI> getAccessedUris()
    {
        if ( accessedUris == null )
        {
            accessedUris = new ArrayList<HttpURI>();
        }
        return accessedUris;
    }

    public void setAccessedUris( List<HttpURI> accessedUris )
    {
        this.accessedUris = accessedUris;
    }

    private List<HttpURI> accessedUris;

    @Override
    public void service( ServletRequest req, ServletResponse res )
        throws ServletException, IOException
    {
        HttpURI uri = ( (Request) req ).getUri();
        getAccessedUris().add( uri );
        super.service( req, res );
    }

}