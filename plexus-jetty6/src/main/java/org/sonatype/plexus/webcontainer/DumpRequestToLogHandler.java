package org.sonatype.plexus.webcontainer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.util.StringUtil;

public class DumpRequestToLogHandler
    extends AbstractHandler
{
    private Logger log = Logger.getLogger( DumpRequestToLogHandler.class.toString() );

    private String label = "Dump HttpHandler";

    public DumpRequestToLogHandler()
    {
    }

    public DumpRequestToLogHandler( String label )
    {
        this.label = label;
    }

    public void handle( String arg0, HttpServletRequest request, HttpServletResponse responseNotUsed, int dispatch )
        throws IOException, ServletException
    {
        Request base_request =
            ( request instanceof Request ) ? (Request) request : HttpConnection.getCurrentConnection().getRequest();

        if ( !isStarted() )
            return;

        base_request.setHandled( true );

//        OutputStream out = response.getOutputStream();
        ByteArrayOutputStream buf = new ByteArrayOutputStream( 2048 );
        Writer writer = new OutputStreamWriter( buf, StringUtil.__ISO_8859_1 );
        writer.write( "Request Dump from:" + label );
        writer.write( "\npathInfo=" + request.getPathInfo() + "\n\n" );
        writer.write( "Header:\n" );
        writer.write( request.toString() );
        writer.write( "\n\nParameters:\n" );
        Enumeration names = request.getParameterNames();
        while ( names.hasMoreElements() )
        {
            String name = names.nextElement().toString();
            String[] values = request.getParameterValues( name );
            if ( values == null || values.length == 0 )
            {
                writer.write( name );
                writer.write( "=\n" );
            }
            else if ( values.length == 1 )
            {
                writer.write( name );
                writer.write( "=" );
                writer.write( values[0] );
                writer.write( "\n" );
            }
            else
            {
                for ( int i = 0; i < values.length; i++ )
                {
                    writer.write( name );
                    writer.write( "[" + i + "]=" );
                    writer.write( values[i] );
                    writer.write( "\n" );
                }
            }
        }

        String cookie_name = request.getParameter( "CookieName" );
        if ( cookie_name != null && cookie_name.trim().length() > 0 )
        {
            String cookie_action = request.getParameter( "Button" );
            try
            {
                Cookie cookie = new Cookie( cookie_name.trim(), request.getParameter( "CookieVal" ) );
                if ( "Clear Cookie".equals( cookie_action ) )
                    cookie.setMaxAge( 0 );
//                response.addCookie( cookie );
            }
            catch ( IllegalArgumentException e )
            {
                writer.write( "BAD Set-Cookie:" );
                writer.write( e.toString() );
            }
        }

        writer.write( "\n\nCookies:\n" );
        Cookie[] cookies = request.getCookies();
        if ( cookies != null && cookies.length > 0 )
        {
            for ( int c = 0; c < cookies.length; c++ )
            {
                Cookie cookie = cookies[c];
                writer.write( cookie.getName() );
                writer.write( "=" );
                writer.write( cookie.getValue() );
                writer.write( "\n" );
            }
        }

        writer.write( "\n\nAttributes:\n" );
        Enumeration attributes = request.getAttributeNames();
        if ( attributes != null && attributes.hasMoreElements() )
        {
            while ( attributes.hasMoreElements() )
            {
                String attr = attributes.nextElement().toString();
                writer.write( attr );
                writer.write( "=" );
                writer.write( request.getAttribute( attr ).toString() );
                writer.write( "\n" );
            }
        }

        writer.write( "\n\nContent:\n" );
        byte[] content = new byte[4096];
        int len;
        try
        {
            InputStream in = request.getInputStream();
            while ( ( len = in.read( content ) ) >= 0 )
                writer.write( new String( content, 0, len ) );
        }
        catch ( IOException e )
        {
            writer.write( e.toString() );
        }

        writer.write( "\n" );

        // commit now
        writer.flush();
        
        System.out.println( "Request: \n" + buf.toString() );

    }

}
