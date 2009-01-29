package org.sonatype.jettytestsuite.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.util.IOUtil;

public class FileServerServlet
    extends GenericServlet
{
    private static final long serialVersionUID = -6702619558275132007L;

    private File content;

    public FileServerServlet( File content )
    {
        this.content = content;
    }

    @Override
    public void service( ServletRequest request, ServletResponse response )
        throws ServletException, IOException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getPathInfo();

        File file = new File( content, path );
        if ( !file.exists() )
        {
            res.sendError( HttpServletResponse.SC_NOT_FOUND, "File not found " + file.getAbsolutePath() );
            return;
        }
        if ( !file.isFile() )
        {
            res.sendError( HttpServletResponse.SC_FORBIDDEN, "Directory not accessible " + file.getAbsolutePath() );
            return;
        }

        InputStream input = new FileInputStream( file );
        OutputStream output = response.getOutputStream();
        IOUtil.copy( input, output );
        IOUtil.close( input );
        IOUtil.close( output );
    }
}