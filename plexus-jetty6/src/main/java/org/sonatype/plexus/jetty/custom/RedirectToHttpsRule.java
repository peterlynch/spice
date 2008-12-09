package org.sonatype.plexus.jetty.custom;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.rewrite.Rule;
import org.mortbay.log.Log;
import org.mortbay.log.Logger;

public class RedirectToHttpsRule
    extends Rule
{

    private Integer httpsPort;
    
    public RedirectToHttpsRule()
    {
        setTerminating( true );
    }

    public int getHttpsPort()
    {
        return httpsPort;
    }

    public void setHttpsPort( int httpsPort )
    {
        Logger logger = Log.getLogger( getClass().getName() );
        logger.info( "HTTPS port set to: {}", httpsPort, null );

        this.httpsPort = httpsPort;
    }

    @Override
    public String matchAndApply( String target, HttpServletRequest request, HttpServletResponse response )
        throws IOException
    {
        Logger logger = Log.getLogger( getClass().getName() );

        StringBuffer requestURL = request.getRequestURL();
        logger.info( "Original URL: {}", requestURL, null );

        if ( !requestURL.toString().startsWith( "https" ) )
        {
            if ( "POST".equals( request.getMethod() ) )
            {
                response.sendError( HttpServletResponse.SC_BAD_REQUEST, "POST to HTTP not supported. Please use HTTPS" + (httpsPort == null ? "" : " (Port: " + httpsPort + ")" ) + " instead." );
                return target;
            }
            
            URL url = new URL( requestURL.toString() );
            
            StringBuilder result = new StringBuilder();
            result.append( "https://" ).append( url.getHost() );
            
            if ( httpsPort != null )
            {
                result.append( ':' ).append( httpsPort );
            }

            result.append( url.getPath() );
            
            String queryString = request.getQueryString();
            if ( queryString != null )
            {
                logger.info( "Adding query string to redirect: {}", queryString, null );
                result.append( '?' ).append( queryString );
            }

            logger.info( "Redirecting to URL: {}", result, null );
            response.sendRedirect( result.toString() );
            return target;
        }
        else
        {
            logger.info( "NOT redirecting. Already HTTPS: {}", requestURL, null );
            return null;
        }
    }

}
