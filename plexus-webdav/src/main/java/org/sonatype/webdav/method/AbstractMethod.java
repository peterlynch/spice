package org.sonatype.webdav.method;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.webdav.Globals;
import org.sonatype.webdav.Method;
import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.Resource;
import org.sonatype.webdav.ResourceCollection;
import org.sonatype.webdav.ResourceException;
import org.sonatype.webdav.UnauthorizedException;
import org.sonatype.webdav.security.Authorization;
import org.sonatype.webdav.security.Permission;
import org.sonatype.webdav.security.User;
import org.sonatype.webdav.util.RequestUtil;
import org.sonatype.webdav.util.ServerInfo;
import org.sonatype.webdav.util.URLEncoder;


/**
 * Created by IntelliJ IDEA.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractMethod
    extends AbstractLogEnabled
    implements Method
{

    /**
     * Array containing the safe characters set.
     */
    protected static URLEncoder urlEncoder;

    /**
     * Full range marker.
     */
    protected static ArrayList FULL = new ArrayList();

    /**
     * The input buffer size to use when serving resourceCollection.
     */
    protected int input = 2048;

    /**
     * The output buffer size to use when serving resourceCollection.
     */
    protected int output = 2048;

    /**
     * Size of file transfer buffer in bytes.
     */
    protected static final int BUFFER_SIZE = 4096;

    protected int debug = 9;

    protected boolean listings = true;

    /**
     * File encoding to be used when reading static files. If none is specified the platform default is used.
     */
    protected String fileEncoding = null;

    /**
     * MIME multipart separation string
     */
    protected static final String mimeSeparation = "CATALINA_MIME_BOUNDARY";

    /**
     * Secret information used to generate reasonably secure lock ids.
     */
    String secret = "sonatype-secret";

    /**
     * Is this webdav read only?
     */
    private boolean readOnly = false;

    /**
     * The collection of resources we are serving
     */
    protected ResourceCollection resourceCollection;

    /**
     * Our authorization
     */
    protected Authorization authz;

    static
    {
        urlEncoder = new URLEncoder();
        urlEncoder.addSafeCharacter( '-' );
        urlEncoder.addSafeCharacter( '_' );
        urlEncoder.addSafeCharacter( '.' );
        urlEncoder.addSafeCharacter( '*' );
        urlEncoder.addSafeCharacter( '/' );
    }

    public void setSecret( String secret )
    {
        this.secret = secret;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly( boolean readOnly )
    {
        this.readOnly = readOnly;
    }

    public int getDebug()
    {
        return debug;
    }

    public void setDebug( int debug )
    {
        this.debug = debug;
    }

    public boolean getListings()
    {
        return listings;
    }

    public void setListings( boolean listings )
    {
        this.listings = listings;
    }

    public void setFileEncoding( String fileEncoding )
    {
        this.fileEncoding = fileEncoding;
    }

    public void setAuthorization( Authorization authz )
    {
        this.authz = authz;
    }

    public void setResourceCollection( ResourceCollection collection )
    {
        this.resourceCollection = collection;
    }

    public boolean authorizeRead( User user, HttpServletResponse res )
        throws IOException
    {
        if ( authz.authorize( user, Permission.PERMISSION_REPOSITORY_READ ) )
        {
            return true;
        }

        res.sendError( HttpServletResponse.SC_FORBIDDEN );
        return false;
    }

    public boolean authorizeWrite( User user, HttpServletResponse res )
        throws IOException
    {
        if ( authz.authorize( user, Permission.PERMISSION_REPOSITORY_WRITE ) )
        {
            return true;
        }

        res.sendError( HttpServletResponse.SC_FORBIDDEN );
        return false;
    }

    /**
     * Serve the specified resource, optionally including the data content.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param content Should the content be included?
     * @throws IOException if an input/output error occurs
     * @throws javax.servlet.ServletException if a servlet-specified error occurs
     */
    protected void serveResource( MethodExecutionContext context, HttpServletRequest request,
        HttpServletResponse response, boolean content )
        throws IOException,
            UnauthorizedException
    {

        // Identify the requested resource path
        String path = getRelativePath( request );

        if ( debug > 0 )
        {
            if ( content )
            {
                getLogger().info( "DefaultServlet.serveResource:  Serving resource '" + path + "' headers and data" );
            }
            else
            {
                getLogger().info( "DefaultServlet.serveResource:  Serving resource '" + path + "' headers only" );
            }
        }

        Object resource = null;
        try
        {
            resource = resourceCollection.lookup( context, path );
        }
        catch ( ResourceException e )
        {
            // handled below
        }

        if ( resource == null || ( resource instanceof Resource && !( (Resource) resource ).getExists() ) )
        {
            // Check if we're included so we can return the appropriate
            // missing resource name in the error
            String requestUri = (String) request.getAttribute( Globals.INCLUDE_REQUEST_URI_ATTR );

            if ( requestUri == null )
            {
                requestUri = request.getRequestURI();
            }
            else
            {
                // We're included, and the response.sendError() below is going
                // to be ignored by the resource that is including us.
                // Therefore, the only way we can let the including resource
                // know is by including warning message in response
                response.getWriter().write( "The requested resource (" );
                response.getWriter().write( requestUri );
                response.getWriter().write( ") is not available" );
            }

            response.sendError( HttpServletResponse.SC_NOT_FOUND, requestUri );

            return;
        }

        // If the resource is not a collection, and the resource path
        // ends with "/" or "\", return NOT FOUND
        if ( resource instanceof Resource )
        {
            if ( path.endsWith( "/" ) || ( path.endsWith( "\\" ) ) )
            {
                // Check if we're included so we can return the appropriate
                // missing resource name in the error
                String requestUri = (String) request.getAttribute( Globals.INCLUDE_REQUEST_URI_ATTR );

                if ( requestUri == null )
                {
                    requestUri = request.getRequestURI();
                }

                response.sendError( HttpServletResponse.SC_NOT_FOUND, requestUri );

                return;
            }
        }

        // Check if the conditions specified in the optional If headers are
        // satisfied.
        if ( resource instanceof Resource )
        {
            // Checking If headers
            boolean included = ( request.getAttribute( Globals.INCLUDE_CONTEXT_PATH_ATTR ) != null );

            if ( !included && !checkIfHeaders( request, response, (Resource) resource ) )
            {
                return;
            }

        }

        // Find content type.
        String contentType = null;

        if ( resource instanceof ResourceCollection )
        {
            contentType = "directory";
        }
        else if ( resource instanceof Resource )
        {
            Resource res = (Resource) resource;
            contentType = res.getMimeType();

            if ( contentType == null )
            {
                contentType = request.getSession().getServletContext().getMimeType( res.getName() );

                res.setMimeType( contentType );
            }
        }

        ArrayList ranges = null;

        long contentLength = -1L;

        if ( resource instanceof ResourceCollection )
        {
            // Skip directory listings if we have been configured to
            // suppress them
            if ( !listings )
            {
                response.sendError( HttpServletResponse.SC_NOT_FOUND, request.getRequestURI() );
                return;
            }
            contentType = "text/html;charset=UTF-8";
        }
        else
        {
            Resource res = (Resource) resource;

            // Parse range specifier
            ranges = parseRange( request, response, res );

            // ETag header
            response.setHeader( "ETag", getETag( res ) );

            // Last-Modified header
            response.setHeader( "Last-Modified", res.getLastModifiedHttp() );

            // Get content length
            contentLength = res.getContentLength();
            // Special case for zero length files, which would cause a
            // (silent) ISE when setting the output buffer size
            if ( contentLength == 0L )
            {
                content = false;
            }
        }

        ServletOutputStream ostream = null;

        PrintWriter writer = null;

        if ( content )
        {

            // Trying to retrieve the servlet output stream

            try
            {
                ostream = response.getOutputStream();
            }
            catch ( IllegalStateException e )
            {
                // If it fails, we try to get a Writer instead if we're
                // trying to serve a text file
                if ( ( contentType == null ) || ( contentType.startsWith( "text" ) )
                    || ( contentType.endsWith( "xml" ) ) )
                {
                    writer = response.getWriter();
                }
                else
                {
                    throw e;
                }
            }

        }

        if ( ( resource instanceof ResourceCollection )
            || ( ( ( ranges == null ) || ( ranges.isEmpty() ) ) && ( request.getHeader( "Range" ) == null ) )
            || ( ranges == FULL ) )
        {

            // Set the appropriate output headers
            if ( contentType != null )
            {
                if ( debug > 0 )
                {
                    getLogger().info( "DefaultServlet.serveFile:  contentType='" + contentType + "'" );
                }
                response.setContentType( contentType );
            }
            if ( ( resource != null ) && ( contentLength >= 0 ) )
            {
                if ( debug > 0 )
                {
                    getLogger().info( "DefaultServlet.serveFile:  contentLength=" + contentLength );
                }
                if ( contentLength < Integer.MAX_VALUE )
                {
                    response.setContentLength( (int) contentLength );
                }
                else
                {
                    // Set the content-length as String to be able to use a long
                    response.setHeader( "content-length", "" + contentLength );
                }
            }

            InputStream renderResult = null;

            if ( resource instanceof ResourceCollection )
            {
                if ( content )
                {
                    getLogger().info( "rendering directory listing" );
                    // Serve the directory browser
                    renderResult = render(
                        context,
                        request.getContextPath() + request.getServletPath(),
                        (ResourceCollection) resource );
                }
            }

            // Copy the input stream to our output stream (if requested)
            if ( content )
            {
                try
                {
                    response.setBufferSize( output );
                }
                catch ( IllegalStateException e )
                {
                    // Silent catch
                }

                if ( resource instanceof Resource && ( (Resource) resource ).streamContent( context ) != null )
                {
                    renderResult = ( (Resource) resource ).streamContent( context );
                }

                if ( ostream != null )
                {
                    copy( renderResult, ostream );
                }

                else
                {
                    copy( renderResult, writer );
                }
            }
        }
        else
        {

            if ( ( ranges == null ) || ( ranges.isEmpty() ) )
            {
                return;
            }

            // Partial content response.

            response.setStatus( HttpServletResponse.SC_PARTIAL_CONTENT );

            if ( ranges.size() == 1 )
            {

                Range range = (Range) ranges.get( 0 );
                response.addHeader( "Content-Range", "bytes " + range.start + "-" + range.end + "/" + range.length );
                long length = range.end - range.start + 1;
                if ( length < Integer.MAX_VALUE )
                {
                    response.setContentLength( (int) length );
                }
                else
                {
                    // Set the content-length as String to be able to use a long
                    response.setHeader( "content-length", "" + length );
                }

                if ( contentType != null )
                {
                    if ( debug > 0 )
                    {
                        getLogger().info( "DefaultServlet.serveFile:  contentType='" + contentType + "'" );
                    }
                    response.setContentType( contentType );
                }

                if ( content )
                {
                    try
                    {
                        response.setBufferSize( output );
                    }
                    catch ( IllegalStateException e )
                    {
                        // Silent catch
                    }
                    if ( ostream != null )
                    {
                        copy( context, (Resource) resource, ostream, range );
                    }
                    else
                    {
                        copy( context, (Resource) resource, writer, range );
                    }
                }

            }
            else
            {

                response.setContentType( "multipart/byteranges; boundary=" + mimeSeparation );

                if ( content )
                {
                    try
                    {
                        response.setBufferSize( output );
                    }
                    catch ( IllegalStateException e )
                    {
                        // Silent catch
                    }
                    if ( ostream != null )
                    {
                        copy( context, (Resource) resource, ostream, ranges.iterator(), contentType );
                    }
                    else
                    {
                        copy( context, (Resource) resource, writer, ranges.iterator(), contentType );
                    }
                }
            }
        }
    }

    /**
     * Return an InputStream to an HTML representation of the contents of this directory.
     * 
     * @param contextPath Context path to which our internal paths are relative
     */
    protected InputStream render( MethodExecutionContext context, String contextPath, ResourceCollection resource )
        throws UnauthorizedException
    {
        String name = resource.getPath();

        // Number of characters to trim from the beginnings of filenames
        int trim = name.length();
        if ( !name.endsWith( "/" ) )
        {
            trim += 1;
        }
        if ( name.equals( "/" ) )
        {
            trim = 1;
        }

        // Prepare a writer to a buffered area
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter osWriter = null;
        try
        {
            osWriter = new OutputStreamWriter( stream, "UTF8" );
        }
        catch ( Exception e )
        {
            // Should never happen
            osWriter = new OutputStreamWriter( stream );
        }
        PrintWriter writer = new PrintWriter( osWriter );

        StringBuffer sb = new StringBuffer();

        // rewriteUrl(contextPath) is expensive. cache result for later reuse
        String rewrittenContextPath = urlEncoder.encode( contextPath );

        // Render the page header
        sb.append( "<html>\r\n" );
        sb.append( "<head>\r\n" );
        sb.append( "<title>Directory Listing For " );
        sb.append( name );
        sb.append( "</title>\r\n" );
        sb.append( "</head>\r\n" );
        sb.append( "<body>" );
        sb.append( "<h1>Directory Listing For " );
        sb.append( name );

        // Render the link to our parent (if required)
        String parentDirectory = name;
        if ( parentDirectory.endsWith( "/" ) )
        {
            parentDirectory = parentDirectory.substring( 0, parentDirectory.length() - 1 );
        }
        int slash = parentDirectory.lastIndexOf( '/' );
        if ( slash >= 0 )
        {
            String parent = name.substring( 0, slash );
            sb.append( " - <a href=\"" );
            sb.append( contextPath );
            if ( !parent.endsWith( "/" ) )
            {
                parent += "/";
            }
            sb.append( urlEncoder.encode( parent ) );
            sb.append( "\">" );
            sb.append( "<b>Up To " );
            sb.append( parent );
            sb.append( "</b>" );
            sb.append( "</a>" );
        }

        sb.append( "</h1>" );
        sb.append( "<HR size=\"1\" noshade=\"noshade\">" );

        sb.append( "<table width=\"100%\" cellspacing=\"0\"" + " cellpadding=\"5\" align=\"center\">\r\n" );

        // Render the column headings
        sb.append( "<tr>\r\n" );
        sb.append( "<td align=\"left\"><font size=\"+1\"><strong>" );
        sb.append( "Filename" );
        sb.append( "</strong></font></td>\r\n" );
        sb.append( "<td align=\"center\"><font size=\"+1\"><strong>" );
        sb.append( "Size" );
        sb.append( "</strong></font></td>\r\n" );
        sb.append( "<td align=\"right\"><font size=\"+1\"><strong>" );
        sb.append( "Last Modified" );
        sb.append( "</strong></font></td>\r\n" );
        sb.append( "</tr>" );

        try
        {
            // Render the directory entries within this directory
            Enumeration enumeration = resourceCollection.list( context, name );
            boolean shade = false;

            while ( enumeration.hasMoreElements() )
            {
                Object childResource = enumeration.nextElement();
                String resourceName;
                if ( childResource instanceof ResourceCollection )
                {
                    resourceName = ( (ResourceCollection) childResource ).getPath().substring( trim );
                }
                else
                {
                    resourceName = ( (Resource) childResource ).getName();
                }

                if ( ( ( childResource instanceof Resource ) && !( (Resource) childResource ).getExists() )
                    || resourceName.equalsIgnoreCase( "WEB-INF" ) || resourceName.equalsIgnoreCase( "META-INF" ) )
                {
                    continue;
                }

                sb.append( "<tr" );
                if ( shade )
                {
                    sb.append( " bgcolor=\"#eeeeee\"" );
                }
                sb.append( ">\r\n" );
                shade = !shade;

                sb.append( "<td align=\"left\">&nbsp;&nbsp;\r\n" );
                sb.append( "<a href=\"" );
                sb.append( rewrittenContextPath );
                sb.append( urlEncoder.encode( name + resourceName ) );
                sb.append( "\"><tt>" );
                sb.append( RequestUtil.filter( resourceName ) );
                sb.append( "</tt></a></td>\r\n" );

                sb.append( "<td align=\"right\"><tt>" );
                if ( childResource instanceof ResourceCollection )
                {
                    sb.append( "&nbsp;" );
                }
                else
                {
                    sb.append( renderSize( ( (Resource) childResource ).getContentLength() ) );
                }
                sb.append( "</tt></td>\r\n" );

                sb.append( "<td align=\"right\"><tt>" );
                if ( childResource instanceof Resource )
                {
                    sb.append( ( (Resource) childResource ).getLastModifiedHttp() );
                }
                else
                {
                    sb.append( ( (ResourceCollection) childResource ).getLastModifiedHttp() );
                }
                sb.append( "</tt></td>\r\n" );

                sb.append( "</tr>\r\n" );
            }

        }
        catch ( ResourceException e )
        {
            getLogger().error( "Got Resource exception", e );
        }

        // Render the page footer
        sb.append( "</table>\r\n" );

        sb.append( "<HR size=\"1\" noshade=\"noshade\">" );

        sb.append( "<h3>" ).append( ServerInfo.getServerInfo() ).append( "</h3>" );
        sb.append( "</body>\r\n" );
        sb.append( "</html>\r\n" );

        // Return an input stream to the underlying bytes
        writer.write( sb.toString() );
        writer.flush();

        return ( new ByteArrayInputStream( stream.toByteArray() ) );

    }

    /**
     * Render the specified file size (in bytes).
     * 
     * @param size File size (in bytes)
     */
    protected String renderSize( long size )
    {

        long leftSide = size / 1024;
        long rightSide = ( size % 1024 ) / 103; // Makes 1 digit
        if ( ( leftSide == 0 ) && ( rightSide == 0 ) && ( size > 0 ) )
        {
            rightSide = 1;
        }

        return ( "" + leftSide + "." + rightSide + " kb" );

    }

    /**
     * Return the relative path associated with this servlet.
     * 
     * @param request The servlet request we are processing
     */
    protected String getRelativePath( HttpServletRequest request )
    {

        // Are we being processed by a RequestDispatcher.include()?
        if ( request.getAttribute( Globals.INCLUDE_REQUEST_URI_ATTR ) != null )
        {
            String result = (String) request.getAttribute( Globals.INCLUDE_PATH_INFO_ATTR );
            if ( result == null )
            {
                result = (String) request.getAttribute( Globals.INCLUDE_SERVLET_PATH_ATTR );
            }
            if ( ( result == null ) || ( result.equals( "" ) ) )
            {
                result = "/";
            }
            return ( result );
        }

        // No, extract the desired path directly from the request
        String result = request.getPathInfo();
        if ( ( result == null ) || ( result.equals( "" ) ) )
        {
            result = "/";
        }
        return ( result );

    }

    /**
     * Check if the conditions specified in the optional If headers are satisfied.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resource The resource information
     * @return boolean true if the resource meets all the specified conditions, and false if any of the conditions is
     *         not satisfied, in which case request processing is stopped
     */
    protected boolean checkIfHeaders( HttpServletRequest request, HttpServletResponse response, Resource resource )
        throws IOException
    {

        return checkIfMatch( request, response, resource ) && checkIfModifiedSince( request, response, resource )
            && checkIfNoneMatch( request, response, resource ) && checkIfUnmodifiedSince( request, response, resource );

    }

    /**
     * Check if the if-match condition is satisfied.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resource File object
     * @return boolean true if the resource meets the specified condition, and false if the condition is not satisfied,
     *         in which case request processing is stopped
     */
    protected boolean checkIfMatch( HttpServletRequest request, HttpServletResponse response, Resource resource )
        throws IOException
    {

        String eTag = getETag( resource );
        String headerValue = request.getHeader( "If-Match" );
        if ( headerValue != null )
        {
            if ( headerValue.indexOf( '*' ) == -1 )
            {

                StringTokenizer commaTokenizer = new StringTokenizer( headerValue, "," );
                boolean conditionSatisfied = false;

                while ( !conditionSatisfied && commaTokenizer.hasMoreTokens() )
                {
                    String currentToken = commaTokenizer.nextToken();
                    if ( currentToken.trim().equals( eTag ) )
                    {
                        conditionSatisfied = true;
                    }
                }

                // If none of the given ETags match, 412 Precodition failed is
                // sent back
                if ( !conditionSatisfied )
                {
                    response.sendError( HttpServletResponse.SC_PRECONDITION_FAILED );
                    return false;
                }

            }
        }
        return true;

    }

    /**
     * Check if the if-modified-since condition is satisfied.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resource File object
     * @return boolean true if the resource meets the specified condition, and false if the condition is not satisfied,
     *         in which case request processing is stopped
     */
    protected boolean checkIfModifiedSince( HttpServletRequest request, HttpServletResponse response, Resource resource )
        throws IOException
    {
        try
        {
            long headerValue = request.getDateHeader( "If-Modified-Since" );
            long lastModified = resource.getLastModified();
            if ( headerValue != -1 )
            {

                // If an If-None-Match header has been specified, if modified since
                // is ignored.
                if ( ( request.getHeader( "If-None-Match" ) == null ) && ( lastModified < headerValue + 1000 ) )
                {
                    // The entity has not been modified since the date
                    // specified by the client. This is not an error case.
                    response.setStatus( HttpServletResponse.SC_NOT_MODIFIED );
                    return false;
                }
            }
        }
        catch ( IllegalArgumentException illegalArgument )
        {
            return true;
        }
        return true;

    }

    /**
     * Check if the if-none-match condition is satisfied.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resource File object
     * @return boolean true if the resource meets the specified condition, and false if the condition is not satisfied,
     *         in which case request processing is stopped
     */
    protected boolean checkIfNoneMatch( HttpServletRequest request, HttpServletResponse response, Resource resource )
        throws IOException
    {

        String eTag = getETag( resource );
        String headerValue = request.getHeader( "If-None-Match" );
        if ( headerValue != null )
        {

            boolean conditionSatisfied = false;

            if ( !headerValue.equals( "*" ) )
            {

                StringTokenizer commaTokenizer = new StringTokenizer( headerValue, "," );

                while ( !conditionSatisfied && commaTokenizer.hasMoreTokens() )
                {
                    String currentToken = commaTokenizer.nextToken();
                    if ( currentToken.trim().equals( eTag ) )
                    {
                        conditionSatisfied = true;
                    }
                }

            }
            else
            {
                conditionSatisfied = true;
            }

            if ( conditionSatisfied )
            {

                // For GET and HEAD, we should respond with
                // 304 Not Modified.
                // For every other method, 412 Precondition Failed is sent
                // back.
                if ( ( "GET".equals( request.getMethod() ) ) || ( "HEAD".equals( request.getMethod() ) ) )
                {
                    response.setStatus( HttpServletResponse.SC_NOT_MODIFIED );
                    return false;
                }
                else
                {
                    response.sendError( HttpServletResponse.SC_PRECONDITION_FAILED );
                    return false;
                }
            }
        }
        return true;

    }

    /**
     * Check if the if-unmodified-since condition is satisfied.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resource File object
     * @return boolean true if the resource meets the specified condition, and false if the condition is not satisfied,
     *         in which case request processing is stopped
     */
    protected boolean checkIfUnmodifiedSince( HttpServletRequest request, HttpServletResponse response,
        Resource resource )
        throws IOException
    {
        try
        {
            long lastModified = resource.getLastModified();
            long headerValue = request.getDateHeader( "If-Unmodified-Since" );
            if ( headerValue != -1 )
            {
                if ( lastModified >= ( headerValue + 1000 ) )
                {
                    // The entity has not been modified since the date
                    // specified by the client. This is not an error case.
                    response.sendError( HttpServletResponse.SC_PRECONDITION_FAILED );
                    return false;
                }
            }
        }
        catch ( IllegalArgumentException illegalArgument )
        {
            return true;
        }
        return true;

    }

    /**
     * Get the ETag associated with a file.
     * 
     * @param resource The resource information
     */
    protected String getETag( Resource resource )
    {
        String result = null;
        if ( ( result = resource.getETag( true ) ) != null )
        {
            return result;
        }
        else if ( ( result = resource.getETag() ) != null )
        {
            return result;
        }
        else
        {
            return "W/\"" + resource.getContentLength() + "-" + resource.getLastModified() + "\"";
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
     * closed before returning (even in the face of an exception).
     * 
     * @param ostream The output stream to write to
     * @throws IOException if an input/output error occurs
     */
    protected void copy( InputStream is, ServletOutputStream ostream )
        throws IOException
    {

        IOException exception = null;

        InputStream istream = new BufferedInputStream( is, input );

        // Copy the input stream to the output stream
        exception = copyRange( istream, ostream );

        // Clean up the input stream
        try
        {
            istream.close();
        }
        catch ( Exception e )
        {
            getLogger().error( "DefaultServlet.copy: exception closing input stream: " + e.getMessage() );
        }

        // Rethrow any exception that has occurred
        if ( exception != null )
        {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
     * closed before returning (even in the face of an exception).
     * 
     * @param writer The writer to write to
     * @throws IOException if an input/output error occurs
     */
    protected void copy( InputStream is, PrintWriter writer )
        throws IOException
    {
        IOException exception;

        Reader reader;
        if ( fileEncoding == null )
        {
            reader = new InputStreamReader( is );
        }
        else
        {
            reader = new InputStreamReader( is, fileEncoding );
        }

        // Copy the input stream to the output stream
        exception = copyRange( reader, writer );

        // Clean up the reader
        try
        {
            reader.close();
        }
        catch ( Exception e )
        {
            getLogger().error( "DefaultServlet.copy: exception closing reader: " + e.getMessage() );
        }

        // Rethrow any exception that has occurred
        if ( exception != null )
        {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
     * closed before returning (even in the face of an exception).
     * 
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @return Exception which occurred during processing
     */
    protected IOException copyRange( InputStream istream, ServletOutputStream ostream )
    {

        // Copy the input stream to the output stream
        IOException exception = null;
        byte buffer[] = new byte[input];
        int len = buffer.length;
        while ( true )
        {
            try
            {
                len = istream.read( buffer );
                if ( len == -1 )
                {
                    break;
                }
                ostream.write( buffer, 0, len );
            }
            catch ( IOException e )
            {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }

    /**
     * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
     * closed before returning (even in the face of an exception).
     * 
     * @param reader The reader to read from
     * @param writer The writer to write to
     * @return Exception which occurred during processing
     */
    protected IOException copyRange( Reader reader, PrintWriter writer )
    {

        // Copy the input stream to the output stream
        IOException exception = null;
        char buffer[] = new char[input];
        int len = buffer.length;
        while ( true )
        {
            try
            {
                len = reader.read( buffer );
                if ( len == -1 )
                {
                    break;
                }
                writer.write( buffer, 0, len );
            }
            catch ( IOException e )
            {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }

    /**
     * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
     * closed before returning (even in the face of an exception).
     * 
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    protected IOException copyRange( InputStream istream, ServletOutputStream ostream, long start, long end )
    {

        if ( debug > 10 )
        {
            getLogger().debug( "Serving bytes:" + start + "-" + end );
        }

        try
        {
            istream.skip( start );
        }
        catch ( IOException e )
        {
            return e;
        }

        IOException exception = null;
        long bytesToRead = end - start + 1;

        byte buffer[] = new byte[input];
        int len = buffer.length;
        while ( ( bytesToRead > 0 ) && ( len >= buffer.length ) )
        {
            try
            {
                len = istream.read( buffer );
                if ( bytesToRead >= len )
                {
                    ostream.write( buffer, 0, len );
                    bytesToRead -= len;
                }
                else
                {
                    ostream.write( buffer, 0, (int) bytesToRead );
                    bytesToRead = 0;
                }
            }
            catch ( IOException e )
            {
                exception = e;
                len = -1;
            }
            if ( len < buffer.length )
            {
                break;
            }
        }

        return exception;

    }

    /**
     * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
     * closed before returning (even in the face of an exception).
     * 
     * @param reader The reader to read from
     * @param writer The writer to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    protected IOException copyRange( Reader reader, PrintWriter writer, long start, long end )
    {

        try
        {
            reader.skip( start );
        }
        catch ( IOException e )
        {
            return e;
        }

        IOException exception = null;
        long bytesToRead = end - start + 1;

        char buffer[] = new char[input];
        int len = buffer.length;
        while ( ( bytesToRead > 0 ) && ( len >= buffer.length ) )
        {
            try
            {
                len = reader.read( buffer );
                if ( bytesToRead >= len )
                {
                    writer.write( buffer, 0, len );
                    bytesToRead -= len;
                }
                else
                {
                    writer.write( buffer, 0, (int) bytesToRead );
                    bytesToRead = 0;
                }
            }
            catch ( IOException e )
            {
                exception = e;
                len = -1;
            }
            if ( len < buffer.length )
            {
                break;
            }
        }

        return exception;

    }

    /**
     * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
     * closed before returning (even in the face of an exception).
     * 
     * @param ostream The output stream to write to
     * @param range Range the client wanted to retrieve
     * @throws IOException if an input/output error occurs
     */
    protected void copy( MethodExecutionContext context, Resource resource, ServletOutputStream ostream, Range range )
        throws IOException
    {

        IOException exception = null;

        InputStream resourceInputStream = resource.streamContent( context );
        InputStream istream = new BufferedInputStream( resourceInputStream, input );
        exception = copyRange( istream, ostream, range.start, range.end );

        // Clean up the input stream
        try
        {
            istream.close();
        }
        catch ( Exception e )
        {
            getLogger().error( "DefaultServlet.copy: exception closing input stream: " + e.getMessage() );
        }

        // Rethrow any exception that has occurred
        if ( exception != null )
        {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
     * closed before returning (even in the face of an exception).
     * 
     * @param writer The writer to write to
     * @param range Range the client wanted to retrieve
     * @throws IOException if an input/output error occurs
     */
    protected void copy( MethodExecutionContext context, Resource resource, PrintWriter writer, Range range )
        throws IOException
    {

        IOException exception = null;

        InputStream resourceInputStream = resource.streamContent( context );

        Reader reader;
        if ( fileEncoding == null )
        {
            reader = new InputStreamReader( resourceInputStream );
        }
        else
        {
            reader = new InputStreamReader( resourceInputStream, fileEncoding );
        }

        exception = copyRange( reader, writer, range.start, range.end );

        // Clean up the input stream
        try
        {
            reader.close();
        }
        catch ( Exception e )
        {
            getLogger().error( "DefaultServlet.copy: exception closing reader: " + e.getMessage() );
        }

        // Rethrow any exception that has occurred
        if ( exception != null )
        {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
     * closed before returning (even in the face of an exception).
     * 
     * @param ostream The output stream to write to
     * @param ranges Enumeration of the ranges the client wanted to retrieve
     * @param contentType Content type of the resource
     * @throws IOException if an input/output error occurs
     */
    protected void copy( MethodExecutionContext context, Resource resource, ServletOutputStream ostream,
        Iterator ranges, String contentType )
        throws IOException
    {

        IOException exception = null;

        while ( ( exception == null ) && ( ranges.hasNext() ) )
        {

            InputStream resourceInputStream = resource.streamContent( context );
            InputStream istream = new BufferedInputStream( resourceInputStream, input );

            Range currentRange = (Range) ranges.next();

            // Writing MIME header.
            ostream.println();
            ostream.println( "--" + mimeSeparation );
            if ( contentType != null )
            {
                ostream.println( "Content-Type: " + contentType );
            }
            ostream.println( "Content-Range: bytes " + currentRange.start + "-" + currentRange.end + "/"
                + currentRange.length );
            ostream.println();

            // Printing content
            exception = copyRange( istream, ostream, currentRange.start, currentRange.end );

            try
            {
                istream.close();
            }
            catch ( Exception e )
            {
                getLogger().error( "DefaultServlet.copy: exception closing input stream: " + e.getMessage() );
            }

        }

        ostream.println();
        ostream.print( "--" + mimeSeparation + "--" );

        // Rethrow any exception that has occurred
        if ( exception != null )
        {
            throw exception;
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
     * closed before returning (even in the face of an exception).
     * 
     * @param writer The writer to write to
     * @param ranges Enumeration of the ranges the client wanted to retrieve
     * @param contentType Content type of the resource
     * @throws IOException if an input/output error occurs
     */
    protected void copy( MethodExecutionContext context, Resource resource, PrintWriter writer, Iterator ranges,
        String contentType )
        throws IOException
    {

        IOException exception = null;

        while ( ( exception == null ) && ( ranges.hasNext() ) )
        {

            InputStream resourceInputStream = resource.streamContent( context );

            Reader reader;
            if ( fileEncoding == null )
            {
                reader = new InputStreamReader( resourceInputStream );
            }
            else
            {
                reader = new InputStreamReader( resourceInputStream, fileEncoding );
            }

            Range currentRange = (Range) ranges.next();

            // Writing MIME header.
            writer.println();
            writer.println( "--" + mimeSeparation );
            if ( contentType != null )
            {
                writer.println( "Content-Type: " + contentType );
            }
            writer.println( "Content-Range: bytes " + currentRange.start + "-" + currentRange.end + "/"
                + currentRange.length );
            writer.println();

            // Printing content
            exception = copyRange( reader, writer, currentRange.start, currentRange.end );

            try
            {
                reader.close();
            }
            catch ( Exception e )
            {
                getLogger().error( "DefaultServlet.copy: exception closing reader: " + e.getMessage() );
            }

        }

        writer.println();
        writer.print( "--" + mimeSeparation + "--" );

        // Rethrow any exception that has occurred
        if ( exception != null )
        {
            throw exception;
        }
    }

    /**
     * Parse the range header.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Vector of ranges
     */
    protected ArrayList parseRange( HttpServletRequest request, HttpServletResponse response, Resource resource )
        throws IOException
    {

        // Checking If-Range
        String headerValue = request.getHeader( "If-Range" );

        if ( headerValue != null )
        {

            long headerValueTime = ( -1L );
            try
            {
                headerValueTime = request.getDateHeader( "If-Range" );
            }
            catch ( Exception e )
            {
                ;
            }

            String eTag = getETag( resource );
            long lastModified = resource.getLastModified();

            if ( headerValueTime == ( -1L ) )
            {

                // If the ETag the client gave does not match the entity
                // etag, then the entire entity is returned.
                if ( !eTag.equals( headerValue.trim() ) )
                {
                    return FULL;
                }

            }
            else
            {

                // If the timestamp of the entity the client got is older than
                // the last modification date of the entity, the entire entity
                // is returned.
                if ( lastModified > ( headerValueTime + 1000 ) )
                {
                    return FULL;
                }

            }

        }

        long fileLength = resource.getContentLength();

        if ( fileLength == 0 )
        {
            return null;
        }

        // Retrieving the range header (if any is specified
        String rangeHeader = request.getHeader( "Range" );

        if ( rangeHeader == null )
        {
            return null;
        }
        // bytes is the only range unit supported (and I don't see the point
        // of adding new ones).
        if ( !rangeHeader.startsWith( "bytes" ) )
        {
            response.addHeader( "Content-Range", "bytes */" + fileLength );
            response.sendError( HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE );
            return null;
        }

        rangeHeader = rangeHeader.substring( 6 );

        // Vector which will contain all the ranges which are successfully
        // parsed.
        ArrayList result = new ArrayList();
        StringTokenizer commaTokenizer = new StringTokenizer( rangeHeader, "," );

        // Parsing the range list
        while ( commaTokenizer.hasMoreTokens() )
        {
            String rangeDefinition = commaTokenizer.nextToken().trim();

            Range currentRange = new Range();
            currentRange.length = fileLength;

            int dashPos = rangeDefinition.indexOf( '-' );

            if ( dashPos == -1 )
            {
                response.addHeader( "Content-Range", "bytes */" + fileLength );
                response.sendError( HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE );
                return null;
            }

            if ( dashPos == 0 )
            {

                try
                {
                    long offset = Long.parseLong( rangeDefinition );
                    currentRange.start = fileLength + offset;
                    currentRange.end = fileLength - 1;
                }
                catch ( NumberFormatException e )
                {
                    response.addHeader( "Content-Range", "bytes */" + fileLength );
                    response.sendError( HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE );
                    return null;
                }

            }
            else
            {

                try
                {
                    currentRange.start = Long.parseLong( rangeDefinition.substring( 0, dashPos ) );
                    if ( dashPos < rangeDefinition.length() - 1 )
                    {
                        currentRange.end = Long.parseLong( rangeDefinition.substring( dashPos + 1, rangeDefinition
                            .length() ) );
                    }
                    else
                    {
                        currentRange.end = fileLength - 1;
                    }
                }
                catch ( NumberFormatException e )
                {
                    response.addHeader( "Content-Range", "bytes */" + fileLength );
                    response.sendError( HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE );
                    return null;
                }

            }

            if ( !currentRange.validate() )
            {
                response.addHeader( "Content-Range", "bytes */" + fileLength );
                response.sendError( HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE );
                return null;
            }

            result.add( currentRange );
        }

        return result;
    }

    /**
     * Parse the content-range header.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Range
     */
    protected Range parseContentRange( HttpServletRequest request, HttpServletResponse response )
        throws IOException
    {

        // Retrieving the content-range header (if any is specified
        String rangeHeader = request.getHeader( "Content-Range" );

        if ( rangeHeader == null )
        {
            return null;
        }

        // bytes is the only range unit supported
        if ( !rangeHeader.startsWith( "bytes" ) )
        {
            response.sendError( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }

        rangeHeader = rangeHeader.substring( 6 ).trim();

        int dashPos = rangeHeader.indexOf( '-' );
        int slashPos = rangeHeader.indexOf( '/' );

        if ( dashPos == -1 )
        {
            response.sendError( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }

        if ( slashPos == -1 )
        {
            response.sendError( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }

        Range range = new Range();

        try
        {
            range.start = Long.parseLong( rangeHeader.substring( 0, dashPos ) );
            range.end = Long.parseLong( rangeHeader.substring( dashPos + 1, slashPos ) );
            range.length = Long.parseLong( rangeHeader.substring( slashPos + 1, rangeHeader.length() ) );
        }
        catch ( NumberFormatException e )
        {
            response.sendError( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }

        if ( !range.validate() )
        {
            response.sendError( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }

        return range;

    }

    /**
     * Handle a partial PUT. New content specified in request is appended to existing content in oldRevisionContent (if
     * present). This code does not support simultaneous partial updates to the same resource.
     */
    protected File executePartialPut( MethodExecutionContext context, HttpServletRequest req, Range range, String path )
        throws IOException,
            UnauthorizedException
    {

        // Append data specified in ranges to existing content for this
        // resource - create a temp. file on the local filesystem to
        // perform this operation
        File tempDir = (File) req.getSession().getServletContext().getAttribute( "javax.servlet.context.tempdir" );
        // Convert all '/' characters to '.' in resourcePath
        String convertedResourcePath = path.replace( '/', '.' );
        File contentFile = new File( tempDir, convertedResourcePath );
        contentFile.createNewFile();

        RandomAccessFile randAccessContentFile = new RandomAccessFile( contentFile, "rw" );

        Resource oldResource = null;
        try
        {
            Object obj = resourceCollection.lookup( context, path );
            if ( obj instanceof Resource )
            {
                oldResource = (Resource) obj;
            }
        }
        catch ( ResourceException e )
        {
            getLogger().error( "DefaultServlet.executePartialPut: couldn't find resource at " + path );
        }

        // Copy data in oldRevisionContent to contentFile
        if ( oldResource != null )
        {
            BufferedInputStream bufOldRevStream = new BufferedInputStream(
                oldResource.streamContent( context ),
                BUFFER_SIZE );

            int numBytesRead;
            byte[] copyBuffer = new byte[BUFFER_SIZE];
            while ( ( numBytesRead = bufOldRevStream.read( copyBuffer ) ) != -1 )
            {
                randAccessContentFile.write( copyBuffer, 0, numBytesRead );
            }

            bufOldRevStream.close();
        }

        randAccessContentFile.setLength( range.length );

        // Append data in request input stream to contentFile
        randAccessContentFile.seek( range.start );
        int numBytesRead;
        byte[] transferBuffer = new byte[BUFFER_SIZE];
        BufferedInputStream requestBufInStream = new BufferedInputStream( req.getInputStream(), BUFFER_SIZE );
        while ( ( numBytesRead = requestBufInStream.read( transferBuffer ) ) != -1 )
        {
            randAccessContentFile.write( transferBuffer, 0, numBytesRead );
        }
        randAccessContentFile.close();
        requestBufInStream.close();

        return contentFile;

    }

    /**
     * Get creation date in ISO format.
     */
    protected String getISOCreationDate( long creationDate )
    {
        StringBuffer creationDateValue = new StringBuffer( RequestUtil.formatHttpDate( new Date( creationDate ) ) );

        return creationDateValue.toString();
    }

    // ------------------------------------------------------ Range Inner Class

    protected class Range
    {

        public long start;

        public long end;

        public long length;

        /**
         * Validate range.
         */
        public boolean validate()
        {
            if ( end >= length )
            {
                end = length - 1;
            }
            return ( ( start >= 0 ) && ( end >= 0 ) && ( start <= end ) && ( length > 0 ) );
        }

        public void recycle()
        {
            start = 0;
            end = 0;
            length = 0;
        }

    }
}
