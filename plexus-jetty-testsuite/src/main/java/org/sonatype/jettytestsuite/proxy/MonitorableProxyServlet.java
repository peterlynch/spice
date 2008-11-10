package org.sonatype.jettytestsuite.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.proxy.AsyncProxyServlet;

import sun.misc.BASE64Decoder;

/**
 *
 */
public class MonitorableProxyServlet
    extends AsyncProxyServlet
{

    private List<String> accessedUris;

    private Map<String, String> authentications;

    private boolean useAuthentication;

    public MonitorableProxyServlet()
    {
        super();
    }

    public MonitorableProxyServlet( boolean useAuthentication, Map<String, String> authentications )
    {
        this();
        this.useAuthentication = useAuthentication;
        this.authentications = authentications;
    }

    private void addUris( ServletRequest req, ServletResponse res )
        throws ServletException, IOException
    {
        String uri = ( (Request) req ).getUri().toString();
        getAccessedUris().add( uri );
        super.service( req, res );
    }

    public List<String> getAccessedUris()
    {
        if ( accessedUris == null )
        {
            accessedUris = new ArrayList<String>();
        }
        return accessedUris;
    }

    public Map<String, String> getAuthentications()
    {
        if ( authentications == null )
        {
            authentications = new HashMap<String, String>();
        }
        return authentications;
    }

    public boolean isUseAuthentication()
    {
        return useAuthentication;
    }

    @Override
    public void service( ServletRequest req, ServletResponse res )
        throws ServletException, IOException
    {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        if ( useAuthentication )
        {
            String proxyAuthorization = request.getHeader( "Proxy-Authorization" );
            if ( proxyAuthorization != null && proxyAuthorization.startsWith( "Basic " ) )
            {
                String proxyAuth = proxyAuthorization.substring( 6 );
                BASE64Decoder dec = new BASE64Decoder();
                byte[] auth = dec.decodeBuffer( proxyAuth );
                String authorization = new String( auth );
                String[] authTokens = authorization.split( ":" );
                String user = authTokens[0];
                String password = authTokens[1];

                String authPass = getAuthentications().get( user );
                if ( password.equals( authPass ) )
                {
                    addUris( req, res );
                    return;
                }
            }

            // Proxy-Authenticate Basic realm="CCProxy Authorization"
            response.addHeader( "Proxy-Authenticate", "Basic realm=\"Jetty Proxy Authorization\"" );
            response.setStatus( HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED );
        }
        else
        {
            addUris( req, res );
        }

    }

    public void setAccessedUris( List<String> accessedUris )
    {
        this.accessedUris = accessedUris;
    }

    public void setAuthentications( Map<String, String> authentications )
    {
        this.authentications = authentications;
    }

    public void setUseAuthentication( boolean useAuthentication )
    {
        this.useAuthentication = useAuthentication;
    }

}