/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.webdav.method;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.Resource;
import org.sonatype.webdav.ResourceCollection;
import org.sonatype.webdav.ResourceException;
import org.sonatype.webdav.UnauthorizedException;
import org.sonatype.webdav.WebdavStatus;
import org.sonatype.webdav.util.MD5Encoder;
import org.sonatype.webdav.util.XMLWriter;

import java.io.IOException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Created by IntelliJ IDEA.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractWebdavMethod
    extends AbstractMethod
{
    private static final Map collectionLocksMap = new Hashtable();

    private static final Map resourceLocksMap = new Hashtable();

    private static final Map lockNullResourcesMap = new Hashtable();

    /**
     * Default depth is infinite.
     */
    protected static final int INFINITY = 3; // To limit tree browsing a bit

    /**
     * Create a new lock.
     */
    protected static final int LOCK_CREATION = 0;

    /**
     * Refresh lock.
     */
    protected static final int LOCK_REFRESH = 1;

    /**
     * Default lock timeout value.
     */
    protected static final int DEFAULT_TIMEOUT = 3600;

    /**
     * Maximum lock timeout.
     */
    protected static final int MAX_TIMEOUT = 604800;

    /**
     * MD5 message digest provider.
     */
    protected static MessageDigest md5Helper;

    /**
     * The MD5 helper object for this class.
     */
    protected static final MD5Encoder md5Encoder = new MD5Encoder();

    /**
     * Vector of the heritable locks. <p/> Key : path <br>
     * Value : LockInfo
     */
    List collectionLocks;

    /**
     * Repository of the locks put on single resourceCollection. <p/> Key : path <br />
     * Value : LockInfo
     */
    Map resourceLocks;

    /**
     * Repository of the lock-null resourceCollection. <p/> Key : path of the collection containing the lock-null
     * resource<br>
     * Value : Vector of lock-null resource which are members of the collection. Each element of the Vector is the path
     * associated with the lock-null resource.
     */
    Map lockNullResources;

    public AbstractWebdavMethod()
    {
        try
        {
            md5Helper = MessageDigest.getInstance( "MD5" );
        }
        catch ( NoSuchAlgorithmException e )
        {
            // TODO - fix this nasty hack
            throw new RuntimeException( "No MD5" );
        }
    }

    // TODO perhaps there is a better way than this, but the locks are associated with the ResourceCollections
    public void setResourceCollection( ResourceCollection collection )
    {
        super.setResourceCollection( collection );

        collectionLocks = (Vector) collectionLocksMap.get( collection );
        if ( collectionLocks == null )
        {
            collectionLocks = new Vector();
            collectionLocksMap.put( collection, collectionLocks );
        }

        resourceLocks = (Map) resourceLocksMap.get( collection );
        if ( resourceLocks == null )
        {
            resourceLocks = new Hashtable();
            resourceLocksMap.put( collection, resourceLocks );
        }

        lockNullResources = (Map) lockNullResourcesMap.get( collection );
        if ( lockNullResources == null )
        {
            lockNullResources = new Hashtable();
            lockNullResourcesMap.put( collection, lockNullResources );
        }
    }

    /**
     * Check to see if a resource is currently write locked. The method will look at the "If" header to make sure the
     * client has give the appropriate lock tokens.
     * 
     * @param req Servlet request
     * @return boolean true if the resource is locked (and no appropriate lock token has been found for at least one of
     *         the non-shared locks which are present on the resource).
     */
    protected boolean isLocked( HttpServletRequest req )
    {
        String path = getRelativePath( req );

        String ifHeader = req.getHeader( "If" );
        if ( ifHeader == null )
        {
            ifHeader = "";
        }

        String lockTokenHeader = req.getHeader( "Lock-Token" );
        if ( lockTokenHeader == null )
        {
            lockTokenHeader = "";
        }

        return isLocked( path, ifHeader + lockTokenHeader );
    }

    /**
     * Check to see if a resource is currently write locked.
     * 
     * @param path Path of the resource
     * @param ifHeader "If" HTTP header which was included in the request
     * @return boolean true if the resource is locked (and no appropriate lock token has been found for at least one of
     *         the non-shared locks which are present on the resource).
     */
    protected boolean isLocked( String path, String ifHeader )
    {
        // Checking resource locks
        LockInfo lock = (LockInfo) resourceLocks.get( path );
        if ( ( lock != null ) && ( lock.hasExpired() ) )
        {
            resourceLocks.remove( path );
        }
        else if ( lock != null )
        {
            // At least one of the tokens of the locks must have been given
            Iterator iter = lock.tokens.iterator();
            boolean tokenMatch = false;
            while ( iter.hasNext() )
            {
                String token = (String) iter.next();
                if ( ifHeader.indexOf( token ) != -1 )
                {
                    tokenMatch = true;
                }
            }

            if ( !tokenMatch )
            {
                return true;
            }
        }

        // Checking inheritable collection locks
        Iterator iter = collectionLocks.iterator();
        while ( iter.hasNext() )
        {
            lock = (LockInfo) iter.next();
            if ( lock.hasExpired() )
            {
                iter.remove();
            }
            else if ( path.startsWith( lock.path ) )
            {
                Iterator tokenIter = lock.tokens.iterator();
                boolean tokenMatch = false;
                while ( tokenIter.hasNext() )
                {
                    String token = (String) tokenIter.next();
                    if ( ifHeader.indexOf( token ) != -1 )
                    {
                        tokenMatch = true;
                    }
                }

                if ( !tokenMatch )
                {
                    return true;
                }
            }
        }

        return false;
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

        if ( !super.checkIfHeaders( request, response, resource ) )
        {
            return false;
        }

        // TODO : Checking the WebDAV If header
        return true;

    }

    /**
     * Send a multistatus element containing a complete error report to the client.
     * 
     * @param req Servlet request
     * @param resp Servlet response
     * @param errors The errors to be displayed
     */
    protected void sendReport( HttpServletRequest req, HttpServletResponse resp, Map errors )
        throws IOException
    {
        // If there is only 1 error we should not pass a multistatus response
        if ( errors.size() == 1 )
        {
            Object key = errors.keySet().iterator().next();

            resp.sendError( ( (Integer) errors.get( key ) ).intValue(), key.toString() );
            return;
        }

        resp.setStatus( WebdavStatus.SC_MULTI_STATUS );

        String absoluteUri = req.getRequestURI();
        String relativePath = getRelativePath( req );

        XMLWriter generatedXML = new XMLWriter();
        generatedXML.writeXMLHeader();

        generatedXML.writeElement( null, "multistatus" + generateNamespaceDeclarations(), XMLWriter.OPENING );

        Iterator iter = errors.keySet().iterator();
        while ( iter.hasNext() )
        {
            String errorPath = (String) iter.next();
            int errorCode = ( (Integer) errors.get( errorPath ) ).intValue();

            generatedXML.writeElement( null, "response", XMLWriter.OPENING );

            generatedXML.writeElement( null, "href", XMLWriter.OPENING );
            String toAppend = errorPath.substring( relativePath.length() );
            if ( !toAppend.startsWith( "/" ) )
            {
                toAppend = "/" + toAppend;
            }
            generatedXML.writeText( absoluteUri + toAppend );
            generatedXML.writeElement( null, "href", XMLWriter.CLOSING );
            generatedXML.writeElement( null, "status", XMLWriter.OPENING );
            generatedXML.writeText( "HTTP/1.1 " + errorCode + " " + WebdavStatus.getStatusText( errorCode ) );
            generatedXML.writeElement( null, "status", XMLWriter.CLOSING );

            generatedXML.writeElement( null, "response", XMLWriter.CLOSING );
        }

        generatedXML.writeElement( null, "multistatus", XMLWriter.CLOSING );

        Writer writer = resp.getWriter();
        writer.write( generatedXML.toString() );
        writer.close();

    }

    /**
     * Generate the namespace declarations.
     */
    protected String generateNamespaceDeclarations()
    {
        return " xmlns=\"DAV:\"";
    }

    /**
     * Return a context-relative path, beginning with a "/", that represents the canonical version of the specified path
     * after ".." and "." elements are resolved out. If the specified path attempts to go outside the boundaries of the
     * current context (i.e. too many ".." path elements are present), return <code>null</code> instead.
     * 
     * @param path Path to be normalized
     */
    protected String normalize( String path )
    {
        if ( path == null )
        {
            return null;
        }

        // Create a place for the normalized path
        String normalized = path;

        if ( normalized.equals( "/." ) )
        {
            return "/";
        }

        // Normalize the slashes and add leading slash if necessary
        if ( normalized.indexOf( '\\' ) >= 0 )
        {
            normalized = normalized.replace( '\\', '/' );
        }

        if ( !normalized.startsWith( "/" ) )
        {
            normalized = "/" + normalized;
        }

        // Resolve occurrences of "//" in the normalized path
        while ( true )
        {
            int index = normalized.indexOf( "//" );
            if ( index < 0 )
            {
                break;
            }
            normalized = normalized.substring( 0, index ) + normalized.substring( index + 1 );
        }

        // Resolve occurrences of "/./" in the normalized path
        while ( true )
        {
            int index = normalized.indexOf( "/./" );
            if ( index < 0 )
            {
                break;
            }
            normalized = normalized.substring( 0, index ) + normalized.substring( index + 2 );
        }

        // Resolve occurrences of "/../" in the normalized path
        while ( true )
        {
            int index = normalized.indexOf( "/../" );
            if ( index < 0 )
            {
                break;
            }
            if ( index == 0 )
            {
                return ( null ); // Trying to go outside our context
            }

            int index2 = normalized.lastIndexOf( '/', index - 1 );
            normalized = normalized.substring( 0, index2 ) + normalized.substring( index + 3 );
        }

        // Return the normalized path that we have completed
        return ( normalized );
    }

    /**
     * Determines the methods normally allowed for the resource.
     */
    protected StringBuffer determineMethodsAllowed( MethodExecutionContext context, ResourceCollection resources,
        HttpServletRequest req )
        throws UnauthorizedException
    {

        StringBuffer methodsAllowed = new StringBuffer();
        boolean exists = true;
        Object object = null;
        try
        {
            String path = getRelativePath( req );

            object = resources.lookup( context, path );
        }
        catch ( ResourceException e )
        {
            exists = false;
        }

        if ( !exists )
        {
            methodsAllowed.append( "OPTIONS, MKCOL, PUT, LOCK" );
            return methodsAllowed;
        }

        methodsAllowed.append( "OPTIONS, GET, HEAD, POST, DELETE, TRACE" );
        methodsAllowed.append( ", PROPPATCH, COPY, MOVE, LOCK, UNLOCK" );

        if ( listings )
        {
            methodsAllowed.append( ", PROPFIND" );
        }

        if ( !( object instanceof ResourceCollection ) )
        {
            methodsAllowed.append( ", PUT" );
        }

        return methodsAllowed;
    }

    /**
     * Return JAXP document builder instance.
     */
    protected DocumentBuilder getDocumentBuilder()
        throws IOException
    {
        DocumentBuilder documentBuilder = null;
        DocumentBuilderFactory documentBuilderFactory = null;
        try
        {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware( true );
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }
        catch ( ParserConfigurationException e )
        {
            throw new IOException( "JAXP initialization failed" );
        }
        return documentBuilder;
    }

    // -------------------------------------------------- LockInfo Inner Class

    /**
     * Holds a lock information.
     */
    protected class LockInfo
    {

        // -------------------------------------------------------- Constructor

        /**
         * Constructor.
         */
        public LockInfo()
        {

        }

        // ------------------------------------------------- Instance Variables

        String path = "/";

        String type = "write";

        String scope = "exclusive";

        int depth = 0;

        String owner = "";

        List tokens = new Vector();

        long expiresAt = 0;

        Date creationDate = new Date();

        // ----------------------------------------------------- Public Methods

        /**
         * Get a String representation of this lock token.
         */
        public String toString()
        {

            String result = "Type:" + type + "\n";
            result += "Scope:" + scope + "\n";
            result += "Depth:" + depth + "\n";
            result += "Owner:" + owner + "\n";
            result += "Expiration:" + getISOCreationDate( expiresAt ) + "\n";

            Iterator iter = tokens.iterator();
            while ( iter.hasNext() )
            {
                result += "Token:" + iter.next() + "\n";
            }
            return result;
        }

        /**
         * Return true if the lock has expired.
         */
        public boolean hasExpired()
        {
            return ( System.currentTimeMillis() > expiresAt );
        }

        /**
         * Return true if the lock is exclusive.
         */
        public boolean isExclusive()
        {

            return ( scope.equals( "exclusive" ) );

        }

        /**
         * Get an XML representation of this lock token. This method will append an XML fragment to the given XML
         * writer.
         */
        public void toXML( XMLWriter generatedXML )
        {

            generatedXML.writeElement( null, "activelock", XMLWriter.OPENING );

            generatedXML.writeElement( null, "locktype", XMLWriter.OPENING );
            generatedXML.writeElement( null, type, XMLWriter.NO_CONTENT );
            generatedXML.writeElement( null, "locktype", XMLWriter.CLOSING );

            generatedXML.writeElement( null, "lockscope", XMLWriter.OPENING );
            generatedXML.writeElement( null, scope, XMLWriter.NO_CONTENT );
            generatedXML.writeElement( null, "lockscope", XMLWriter.CLOSING );

            generatedXML.writeElement( null, "depth", XMLWriter.OPENING );
            if ( depth == INFINITY )
            {
                generatedXML.writeText( "Infinity" );
            }
            else
            {
                generatedXML.writeText( "0" );
            }
            generatedXML.writeElement( null, "depth", XMLWriter.CLOSING );

            generatedXML.writeElement( null, "owner", XMLWriter.OPENING );
            generatedXML.writeText( owner );
            generatedXML.writeElement( null, "owner", XMLWriter.CLOSING );

            generatedXML.writeElement( null, "timeout", XMLWriter.OPENING );
            long timeout = ( expiresAt - System.currentTimeMillis() ) / 1000;
            generatedXML.writeText( "Second-" + timeout );
            generatedXML.writeElement( null, "timeout", XMLWriter.CLOSING );

            generatedXML.writeElement( null, "locktoken", XMLWriter.OPENING );

            Iterator iter = tokens.iterator();
            while ( iter.hasNext() )
            {
                generatedXML.writeElement( null, "href", XMLWriter.OPENING );
                generatedXML.writeText( "opaquelocktoken:" + iter.next() );
                generatedXML.writeElement( null, "href", XMLWriter.CLOSING );
            }
            generatedXML.writeElement( null, "locktoken", XMLWriter.CLOSING );

            generatedXML.writeElement( null, "activelock", XMLWriter.CLOSING );

        }
    }
}
