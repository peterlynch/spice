/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package org.sonatype.webdav.method;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.Resource;
import org.sonatype.webdav.ResourceCollection;
import org.sonatype.webdav.ResourceException;
import org.sonatype.webdav.UnauthorizedException;
import org.sonatype.webdav.WebdavStatus;
import org.sonatype.webdav.security.User;
import org.sonatype.webdav.util.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Webdav PROPFIND method.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="propfind"
 * @since 1.0
 */
public class PropFind
    extends AbstractWebdavMethod
{

    /**
     * PROPFIND - Specify a property mask.
     */
    private static final int FIND_BY_PROPERTY = 0;

    /**
     * PROPFIND - Display all properties.
     */
    private static final int FIND_ALL_PROP = 1;

    /**
     * PROPFIND - Return property names.
     */
    private static final int FIND_PROPERTY_NAMES = 2;

    public void execute( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse res )
        throws IOException,
            UnauthorizedException
    {
        if ( !authorizeRead( context.getUser(), res ) )
        {
            return;
        }

        if ( !listings )
        {
            // Get allowed methods
            StringBuffer methodsAllowed = determineMethodsAllowed( context, resourceCollection, req );

            res.addHeader( "Allow", methodsAllowed.toString() );
            res.sendError( WebdavStatus.SC_METHOD_NOT_ALLOWED );
            return;
        }

        String path = getRelativePath( req );
        if ( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }

        if ( ( path.toUpperCase().startsWith( "/WEB-INF" ) ) || ( path.toUpperCase().startsWith( "/META-INF" ) ) )
        {
            res.sendError( WebdavStatus.SC_FORBIDDEN );
            return;
        }

        // Properties which are to be displayed.
        List properties = null;
        // Propfind depth
        int depth = INFINITY;
        // Propfind type
        int type = FIND_ALL_PROP;

        String depthStr = req.getHeader( "Depth" );

        if ( depthStr == null )
        {
            depth = INFINITY;
        }
        else
        {
            if ( depthStr.equals( "0" ) )
            {
                depth = 0;
            }
            else if ( depthStr.equals( "1" ) )
            {
                depth = 1;
            }
            else if ( depthStr.equals( "infinity" ) )
            {
                depth = INFINITY;
            }
        }

        Node propNode = null;

        DocumentBuilder documentBuilder = getDocumentBuilder();

        try
        {
            Document document = documentBuilder.parse( new InputSource( req.getInputStream() ) );

            // Get the root element of the document
            Element rootElement = document.getDocumentElement();
            NodeList childList = rootElement.getChildNodes();

            for ( int i = 0; i < childList.getLength(); i++ )
            {
                Node currentNode = childList.item( i );
                switch ( currentNode.getNodeType() )
                {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        if ( currentNode.getNodeName().endsWith( "prop" ) )
                        {
                            type = FIND_BY_PROPERTY;
                            propNode = currentNode;
                        }
                        if ( currentNode.getNodeName().endsWith( "propname" ) )
                        {
                            type = FIND_PROPERTY_NAMES;
                        }
                        if ( currentNode.getNodeName().endsWith( "allprop" ) )
                        {
                            type = FIND_ALL_PROP;
                        }
                        break;
                }
            }
        }
        catch ( Exception e )
        {
            // Most likely there was no content : we use the defaults.
            // TODO : Enhance that !
        }

        if ( type == FIND_BY_PROPERTY )
        {
            properties = new Vector();
            NodeList childList = propNode.getChildNodes();

            for ( int i = 0; i < childList.getLength(); i++ )
            {
                Node currentNode = childList.item( i );
                switch ( currentNode.getNodeType() )
                {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        String nodeName = currentNode.getNodeName();
                        String propertyName = null;
                        if ( nodeName.indexOf( ':' ) != -1 )
                        {
                            propertyName = nodeName.substring( nodeName.indexOf( ':' ) + 1 );
                        }
                        else
                        {
                            propertyName = nodeName;
                        }
                        // href is a live property which is handled differently
                        properties.add( propertyName );
                        break;
                }
            }

        }

        boolean exists = true;
        Object object = null;
        try
        {
            object = resourceCollection.lookup( context, path );
        }
        catch ( ResourceException e )
        {
            exists = false;
            int slash = path.lastIndexOf( '/' );
            if ( slash != -1 )
            {
                String parentPath = path.substring( 0, slash );
                List currentLockNullResources = (List) lockNullResources.get( parentPath );
                if ( currentLockNullResources != null )
                {
                    Iterator iter = currentLockNullResources.iterator();
                    while ( iter.hasNext() )
                    {
                        String lockNullPath = (String) iter.next();
                        if ( lockNullPath.equals( path ) )
                        {
                            res.setStatus( WebdavStatus.SC_MULTI_STATUS );
                            res.setContentType( "text/xml; charset=UTF-8" );
                            // Create multistatus object
                            XMLWriter generatedXML = new XMLWriter( res.getWriter() );
                            generatedXML.writeXMLHeader();
                            generatedXML.writeElement(
                                null,
                                "multistatus" + generateNamespaceDeclarations(),
                                XMLWriter.OPENING );
                            parseLockNullProperties( req, generatedXML, lockNullPath, type, properties );
                            generatedXML.writeElement( null, "multistatus", XMLWriter.CLOSING );
                            generatedXML.sendData();
                            return;
                        }
                    }
                }
            }
        }

        if ( !exists )
        {
            res.sendError( HttpServletResponse.SC_NOT_FOUND, path );
            return;
        }

        res.setStatus( WebdavStatus.SC_MULTI_STATUS );

        res.setContentType( "text/xml; charset=UTF-8" );

        // Create multistatus object
        XMLWriter generatedXML = new XMLWriter( res.getWriter() );
        generatedXML.writeXMLHeader();

        generatedXML.writeElement( null, "multistatus" + generateNamespaceDeclarations(), XMLWriter.OPENING );

        if ( depth == 0 )
        {
            parseProperties( context, req, generatedXML, path, type, properties );
        }
        else
        {
            // The stack always contains the object of the current level
            Stack stack = new Stack();
            stack.push( path );

            // Stack of the objects one level below
            Stack stackBelow = new Stack();

            while ( ( !stack.isEmpty() ) && ( depth >= 0 ) )
            {
                String currentPath = (String) stack.pop();
                parseProperties( context, req, generatedXML, currentPath, type, properties );

                try
                {
                    object = resourceCollection.lookup( context, currentPath );
                }
                catch ( ResourceException e )
                {
                    continue;
                }

                if ( ( object instanceof ResourceCollection ) && ( depth > 0 ) )
                {

                    try
                    {
                        Enumeration enumeration = resourceCollection.list( context, currentPath );
                        while ( enumeration.hasMoreElements() )
                        {
                            Object ncPair = enumeration.nextElement();
                            String newPath = currentPath;
                            if ( !( newPath.endsWith( "/" ) ) )
                            {
                                newPath += "/";
                            }

                            if ( ncPair instanceof ResourceCollection )
                            {
                                newPath = ( (ResourceCollection) ncPair ).getPath();
                            }
                            else
                            {
                                newPath += ( (Resource) ncPair ).getName();
                            }
                            stackBelow.push( newPath );
                        }
                    }
                    catch ( ResourceException e )
                    {
                        getLogger().error(
                            "WebdavServlet: org.sonatype.webdav.ResourceException processing " + currentPath );
                        res.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, path );
                        return;
                    }

                    // Displaying the lock-null resourceCollection present in that
                    // collection
                    String lockPath = currentPath;
                    if ( lockPath.endsWith( "/" ) )
                    {
                        lockPath = lockPath.substring( 0, lockPath.length() - 1 );
                    }
                    List currentLockNullResources = (List) lockNullResources.get( lockPath );
                    if ( currentLockNullResources != null )
                    {
                        Iterator iter = currentLockNullResources.iterator();
                        while ( iter.hasNext() )
                        {
                            String lockNullPath = (String) iter.next();
                            parseLockNullProperties( req, generatedXML, lockNullPath, type, properties );
                        }
                    }

                }

                if ( stack.isEmpty() )
                {
                    depth--;
                    stack = stackBelow;
                    stackBelow = new Stack();
                }

                generatedXML.sendData();

            }
        }

        generatedXML.writeElement( null, "multistatus", XMLWriter.CLOSING );

        generatedXML.sendData();
    }

    /**
     * Propfind helper method.
     * 
     * @param req The servlet request
     * @param generatedXML XML response to the Propfind request
     * @param path Path of the current resource
     * @param type Propfind type
     * @param propertiesVector If the propfind type is find properties by name, then this Vector contains those
     *        properties
     */
    private void parseProperties( MethodExecutionContext context, HttpServletRequest req, XMLWriter generatedXML,
        String path, int type, List propertiesVector )
        throws UnauthorizedException
    {

        // Exclude any resource in the /WEB-INF and /META-INF subdirectories
        // (the "toUpperCase()" avoids problems on Windows systems)
        if ( path.toUpperCase().startsWith( "/WEB-INF" ) || path.toUpperCase().startsWith( "/META-INF" ) )
        {
            return;
        }

        Object cacheEntry;
        try
        {
            cacheEntry = resourceCollection.lookup( context, path );
        }
        catch ( ResourceException e )
        {
            // not going to happen
            return;
        }

        generatedXML.writeElement( null, "response", XMLWriter.OPENING );
        String status = new String( "HTTP/1.1 " + WebdavStatus.SC_OK + " "
            + WebdavStatus.getStatusText( WebdavStatus.SC_OK ) );

        // Generating href element
        generatedXML.writeElement( null, "href", XMLWriter.OPENING );

        String href = req.getContextPath() + req.getServletPath();
        if ( ( href.endsWith( "/" ) ) && ( path.startsWith( "/" ) ) )
        {
            href += path.substring( 1 );
        }
        else
        {
            href += path;
        }
        if ( ( cacheEntry instanceof ResourceCollection ) && ( !href.endsWith( "/" ) ) )
        {
            href += "/";
        }

        generatedXML.writeText( urlEncoder.encode( href ) );

        generatedXML.writeElement( null, "href", XMLWriter.CLOSING );

        String resourceName = path;
        int lastSlash = path.lastIndexOf( '/' );
        if ( lastSlash != -1 )
        {
            resourceName = resourceName.substring( lastSlash + 1 );
        }

        switch ( type )
        {

            case FIND_ALL_PROP:

                generatedXML.writeElement( null, "propstat", XMLWriter.OPENING );
                generatedXML.writeElement( null, "prop", XMLWriter.OPENING );

                if ( cacheEntry instanceof Resource )
                {
                    generatedXML.writeProperty( null, "creationdate", getISOCreationDate( ( (Resource) cacheEntry )
                        .getCreation() ) );
                }
                else
                {
                    generatedXML.writeProperty(
                        null,
                        "creationdate",
                        getISOCreationDate( ( (ResourceCollection) cacheEntry ).getCreation() ) );
                }
                generatedXML.writeElement( null, "displayname", XMLWriter.OPENING );
                generatedXML.writeData( resourceName );
                generatedXML.writeElement( null, "displayname", XMLWriter.CLOSING );
                if ( !( cacheEntry instanceof ResourceCollection ) )
                {
                    Resource resource = (Resource) cacheEntry;
                    generatedXML.writeProperty(
                        null,
                        "getlastmodified",
                        getISOCreationDate( resource.getLastModified() ) );
                    generatedXML
                        .writeProperty( null, "getcontentlength", String.valueOf( resource.getContentLength() ) );
                    String contentType = req.getSession().getServletContext().getMimeType( resource.getName() );
                    if ( contentType != null )
                    {
                        generatedXML.writeProperty( null, "getcontenttype", contentType );
                    }
                    generatedXML.writeProperty( null, "getetag", getETag( resource ) );
                    generatedXML.writeElement( null, "resourcetype", XMLWriter.NO_CONTENT );
                }
                else
                {
                    generatedXML.writeElement( null, "resourcetype", XMLWriter.OPENING );
                    generatedXML.writeElement( null, "collection", XMLWriter.NO_CONTENT );
                    generatedXML.writeElement( null, "resourcetype", XMLWriter.CLOSING );
                }

                generatedXML.writeProperty( null, "source", "" );

                String supportedLocks = "<lockentry>" + "<lockscope><exclusive/></lockscope>"
                    + "<locktype><write/></locktype>" + "</lockentry>" + "<lockentry>"
                    + "<lockscope><shared/></lockscope>" + "<locktype><write/></locktype>" + "</lockentry>";
                generatedXML.writeElement( null, "supportedlock", XMLWriter.OPENING );
                generatedXML.writeText( supportedLocks );
                generatedXML.writeElement( null, "supportedlock", XMLWriter.CLOSING );

                generateLockDiscovery( path, generatedXML );

                generatedXML.writeElement( null, "prop", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "status", XMLWriter.OPENING );
                generatedXML.writeText( status );
                generatedXML.writeElement( null, "status", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "propstat", XMLWriter.CLOSING );

                break;

            case FIND_PROPERTY_NAMES:

                generatedXML.writeElement( null, "propstat", XMLWriter.OPENING );
                generatedXML.writeElement( null, "prop", XMLWriter.OPENING );

                generatedXML.writeElement( null, "creationdate", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "displayname", XMLWriter.NO_CONTENT );
                if ( cacheEntry != null )
                {
                    generatedXML.writeElement( null, "getcontentlanguage", XMLWriter.NO_CONTENT );
                    generatedXML.writeElement( null, "getcontentlength", XMLWriter.NO_CONTENT );
                    generatedXML.writeElement( null, "getcontenttype", XMLWriter.NO_CONTENT );
                    generatedXML.writeElement( null, "getetag", XMLWriter.NO_CONTENT );
                    generatedXML.writeElement( null, "getlastmodified", XMLWriter.NO_CONTENT );
                }
                generatedXML.writeElement( null, "resourcetype", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "source", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "lockdiscovery", XMLWriter.NO_CONTENT );

                generatedXML.writeElement( null, "prop", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "status", XMLWriter.OPENING );
                generatedXML.writeText( status );
                generatedXML.writeElement( null, "status", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "propstat", XMLWriter.CLOSING );

                break;

            case FIND_BY_PROPERTY:

                List propertiesNotFound = new Vector();

                // Parse the list of properties

                generatedXML.writeElement( null, "propstat", XMLWriter.OPENING );
                generatedXML.writeElement( null, "prop", XMLWriter.OPENING );

                Iterator iter = propertiesVector.iterator();
                while ( iter.hasNext() )
                {
                    String property = (String) iter.next();

                    if ( property.equals( "creationdate" ) )
                    {
                        if ( cacheEntry instanceof ResourceCollection )
                        {
                            generatedXML.writeProperty(
                                null,
                                "creationdate",
                                getISOCreationDate( ( (ResourceCollection) cacheEntry ).getCreation() ) );
                        }
                        else
                        {
                            generatedXML.writeProperty(
                                null,
                                "creationdate",
                                getISOCreationDate( ( (Resource) cacheEntry ).getCreation() ) );
                        }
                    }
                    else if ( property.equals( "displayname" ) )
                    {
                        generatedXML.writeElement( null, "displayname", XMLWriter.OPENING );
                        generatedXML.writeData( resourceName );
                        generatedXML.writeElement( null, "displayname", XMLWriter.CLOSING );
                    }
                    else if ( property.equals( "getcontentlanguage" ) )
                    {
                        if ( cacheEntry instanceof ResourceCollection )
                        {
                            propertiesNotFound.add( property );
                        }
                        else
                        {
                            generatedXML.writeElement( null, "getcontentlanguage", XMLWriter.NO_CONTENT );
                        }
                    }
                    else if ( property.equals( "getcontentlength" ) )
                    {
                        if ( cacheEntry instanceof ResourceCollection )
                        {
                            propertiesNotFound.add( property );
                        }
                        else
                        {
                            generatedXML.writeProperty( null, "getcontentlength", ( String
                                .valueOf( ( (Resource) cacheEntry ).getContentLength() ) ) );
                        }
                    }
                    else if ( property.equals( "getcontenttype" ) )
                    {
                        if ( cacheEntry instanceof ResourceCollection )
                        {
                            propertiesNotFound.add( property );
                        }
                        else
                        {
                            generatedXML.writeProperty( null, "getcontenttype", req
                                .getSession().getServletContext().getMimeType( ( (Resource) cacheEntry ).getName() ) );
                        }
                    }
                    else if ( property.equals( "getetag" ) )
                    {
                        if ( cacheEntry instanceof ResourceCollection )
                        {
                            propertiesNotFound.add( property );
                        }
                        else
                        {
                            generatedXML.writeProperty( null, "getetag", getETag( (Resource) cacheEntry ) );
                        }
                    }
                    else if ( property.equals( "getlastmodified" ) )
                    {
                        if ( cacheEntry instanceof ResourceCollection )
                        {
                            propertiesNotFound.add( property );
                        }
                        else
                        {
                            generatedXML.writeProperty(
                                null,
                                "getlastmodified",
                                getISOCreationDate( ( (Resource) cacheEntry ).getLastModified() ) );
                        }
                    }
                    else if ( property.equals( "resourcetype" ) )
                    {
                        if ( cacheEntry instanceof ResourceCollection )
                        {
                            generatedXML.writeElement( null, "resourcetype", XMLWriter.OPENING );
                            generatedXML.writeElement( null, "collection", XMLWriter.NO_CONTENT );
                            generatedXML.writeElement( null, "resourcetype", XMLWriter.CLOSING );
                        }
                        else
                        {
                            generatedXML.writeElement( null, "resourcetype", XMLWriter.NO_CONTENT );
                        }
                    }
                    else if ( property.equals( "source" ) )
                    {
                        generatedXML.writeProperty( null, "source", "" );
                    }
                    else if ( property.equals( "supportedlock" ) )
                    {
                        supportedLocks = "<lockentry>" + "<lockscope><exclusive/></lockscope>"
                            + "<locktype><write/></locktype>" + "</lockentry>" + "<lockentry>"
                            + "<lockscope><shared/></lockscope>" + "<locktype><write/></locktype>" + "</lockentry>";
                        generatedXML.writeElement( null, "supportedlock", XMLWriter.OPENING );
                        generatedXML.writeText( supportedLocks );
                        generatedXML.writeElement( null, "supportedlock", XMLWriter.CLOSING );
                    }
                    else if ( property.equals( "lockdiscovery" ) )
                    {
                        if ( !generateLockDiscovery( path, generatedXML ) )
                        {
                            propertiesNotFound.add( property );
                        }
                    }
                    else
                    {
                        propertiesNotFound.add( property );
                    }

                }

                generatedXML.writeElement( null, "prop", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "status", XMLWriter.OPENING );
                generatedXML.writeText( status );
                generatedXML.writeElement( null, "status", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "propstat", XMLWriter.CLOSING );

                if ( propertiesNotFound.size() > 0 )
                {
                    status = new String( "HTTP/1.1 " + WebdavStatus.SC_NOT_FOUND + " "
                        + WebdavStatus.getStatusText( WebdavStatus.SC_NOT_FOUND ) );

                    generatedXML.writeElement( null, "propstat", XMLWriter.OPENING );
                    generatedXML.writeElement( null, "prop", XMLWriter.OPENING );

                    Iterator notFoundIter = propertiesNotFound.iterator();
                    while ( notFoundIter.hasNext() )
                    {
                        generatedXML.writeElement( null, (String) notFoundIter.next(), XMLWriter.NO_CONTENT );
                    }

                    generatedXML.writeElement( null, "prop", XMLWriter.CLOSING );
                    generatedXML.writeElement( null, "status", XMLWriter.OPENING );
                    generatedXML.writeText( status );
                    generatedXML.writeElement( null, "status", XMLWriter.CLOSING );
                    generatedXML.writeElement( null, "propstat", XMLWriter.CLOSING );

                }

                break;

        }

        generatedXML.writeElement( null, "response", XMLWriter.CLOSING );

    }

    /**
     * Propfind helper method. Dispays the properties of a lock-null resource.
     * 
     * @param generatedXML XML response to the Propfind request
     * @param path Path of the current resource
     * @param type Propfind type
     * @param propertiesVector If the propfind type is find properties by name, then this Vector contains those
     *        properties
     */
    private void parseLockNullProperties( HttpServletRequest req, XMLWriter generatedXML, String path, int type,
        List propertiesVector )
    {

        // Exclude any resource in the /WEB-INF and /META-INF subdirectories
        // (the "toUpperCase()" avoids problems on Windows systems)
        if ( path.toUpperCase().startsWith( "/WEB-INF" ) || path.toUpperCase().startsWith( "/META-INF" ) )
        {
            return;
        }

        // Retrieving the lock associated with the lock-null resource
        LockInfo lock = (LockInfo) resourceLocks.get( path );

        if ( lock == null )
        {
            return;
        }

        generatedXML.writeElement( null, "response", XMLWriter.OPENING );
        String status = new String( "HTTP/1.1 " + WebdavStatus.SC_OK + " "
            + WebdavStatus.getStatusText( WebdavStatus.SC_OK ) );

        // Generating href element
        generatedXML.writeElement( null, "href", XMLWriter.OPENING );

        String absoluteUri = req.getRequestURI();
        String relativePath = getRelativePath( req );
        String toAppend = path.substring( relativePath.length() );
        if ( !toAppend.startsWith( "/" ) )
        {
            toAppend = "/" + toAppend;
        }

        generatedXML.writeText( urlEncoder.encode( normalize( absoluteUri + toAppend ) ) );

        generatedXML.writeElement( null, "href", XMLWriter.CLOSING );

        String resourceName = path;
        int lastSlash = path.lastIndexOf( '/' );
        if ( lastSlash != -1 )
        {
            resourceName = resourceName.substring( lastSlash + 1 );
        }

        switch ( type )
        {

            case FIND_ALL_PROP:

                generatedXML.writeElement( null, "propstat", XMLWriter.OPENING );
                generatedXML.writeElement( null, "prop", XMLWriter.OPENING );

                generatedXML.writeProperty( null, "creationdate", getISOCreationDate( lock.creationDate.getTime() ) );
                generatedXML.writeElement( null, "displayname", XMLWriter.OPENING );
                generatedXML.writeData( resourceName );
                generatedXML.writeElement( null, "displayname", XMLWriter.CLOSING );
                generatedXML.writeProperty( null, "getlastmodified", getISOCreationDate( lock.creationDate.getTime() ) );
                generatedXML.writeProperty( null, "getcontentlength", String.valueOf( 0 ) );
                generatedXML.writeProperty( null, "getcontenttype", "" );
                generatedXML.writeProperty( null, "getetag", "" );
                generatedXML.writeElement( null, "resourcetype", XMLWriter.OPENING );
                generatedXML.writeElement( null, "lock-null", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "resourcetype", XMLWriter.CLOSING );

                generatedXML.writeProperty( null, "source", "" );

                String supportedLocks = "<lockentry>" + "<lockscope><exclusive/></lockscope>"
                    + "<locktype><write/></locktype>" + "</lockentry>" + "<lockentry>"
                    + "<lockscope><shared/></lockscope>" + "<locktype><write/></locktype>" + "</lockentry>";
                generatedXML.writeElement( null, "supportedlock", XMLWriter.OPENING );
                generatedXML.writeText( supportedLocks );
                generatedXML.writeElement( null, "supportedlock", XMLWriter.CLOSING );

                generateLockDiscovery( path, generatedXML );

                generatedXML.writeElement( null, "prop", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "status", XMLWriter.OPENING );
                generatedXML.writeText( status );
                generatedXML.writeElement( null, "status", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "propstat", XMLWriter.CLOSING );

                break;

            case FIND_PROPERTY_NAMES:

                generatedXML.writeElement( null, "propstat", XMLWriter.OPENING );
                generatedXML.writeElement( null, "prop", XMLWriter.OPENING );

                generatedXML.writeElement( null, "creationdate", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "displayname", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "getcontentlanguage", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "getcontentlength", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "getcontenttype", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "getetag", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "getlastmodified", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "resourcetype", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "source", XMLWriter.NO_CONTENT );
                generatedXML.writeElement( null, "lockdiscovery", XMLWriter.NO_CONTENT );

                generatedXML.writeElement( null, "prop", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "status", XMLWriter.OPENING );
                generatedXML.writeText( status );
                generatedXML.writeElement( null, "status", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "propstat", XMLWriter.CLOSING );

                break;

            case FIND_BY_PROPERTY:

                List propertiesNotFound = new Vector();

                // Parse the list of properties

                generatedXML.writeElement( null, "propstat", XMLWriter.OPENING );
                generatedXML.writeElement( null, "prop", XMLWriter.OPENING );

                Iterator iter = propertiesVector.iterator();
                while ( iter.hasNext() )
                {
                    String property = (String) iter.next();

                    if ( property.equals( "creationdate" ) )
                    {
                        generatedXML.writeProperty( null, "creationdate", getISOCreationDate( lock.creationDate
                            .getTime() ) );
                    }
                    else if ( property.equals( "displayname" ) )
                    {
                        generatedXML.writeElement( null, "displayname", XMLWriter.OPENING );
                        generatedXML.writeData( resourceName );
                        generatedXML.writeElement( null, "displayname", XMLWriter.CLOSING );
                    }
                    else if ( property.equals( "getcontentlanguage" ) )
                    {
                        generatedXML.writeElement( null, "getcontentlanguage", XMLWriter.NO_CONTENT );
                    }
                    else if ( property.equals( "getcontentlength" ) )
                    {
                        generatedXML.writeProperty( null, "getcontentlength", ( String.valueOf( 0 ) ) );
                    }
                    else if ( property.equals( "getcontenttype" ) )
                    {
                        generatedXML.writeProperty( null, "getcontenttype", "" );
                    }
                    else if ( property.equals( "getetag" ) )
                    {
                        generatedXML.writeProperty( null, "getetag", "" );
                    }
                    else if ( property.equals( "getlastmodified" ) )
                    {
                        generatedXML.writeProperty( null, "getlastmodified", getISOCreationDate( lock.creationDate
                            .getTime() ) );
                    }
                    else if ( property.equals( "resourcetype" ) )
                    {
                        generatedXML.writeElement( null, "resourcetype", XMLWriter.OPENING );
                        generatedXML.writeElement( null, "lock-null", XMLWriter.NO_CONTENT );
                        generatedXML.writeElement( null, "resourcetype", XMLWriter.CLOSING );
                    }
                    else if ( property.equals( "source" ) )
                    {
                        generatedXML.writeProperty( null, "source", "" );
                    }
                    else if ( property.equals( "supportedlock" ) )
                    {
                        supportedLocks = "<lockentry>" + "<lockscope><exclusive/></lockscope>"
                            + "<locktype><write/></locktype>" + "</lockentry>" + "<lockentry>"
                            + "<lockscope><shared/></lockscope>" + "<locktype><write/></locktype>" + "</lockentry>";
                        generatedXML.writeElement( null, "supportedlock", XMLWriter.OPENING );
                        generatedXML.writeText( supportedLocks );
                        generatedXML.writeElement( null, "supportedlock", XMLWriter.CLOSING );
                    }
                    else if ( property.equals( "lockdiscovery" ) )
                    {
                        if ( !generateLockDiscovery( path, generatedXML ) )
                        {
                            propertiesNotFound.add( property );
                        }
                    }
                    else
                    {
                        propertiesNotFound.add( property );
                    }

                }

                generatedXML.writeElement( null, "prop", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "status", XMLWriter.OPENING );
                generatedXML.writeText( status );
                generatedXML.writeElement( null, "status", XMLWriter.CLOSING );
                generatedXML.writeElement( null, "propstat", XMLWriter.CLOSING );

                if ( propertiesNotFound.size() > 0 )
                {
                    status = new String( "HTTP/1.1 " + WebdavStatus.SC_NOT_FOUND + " "
                        + WebdavStatus.getStatusText( WebdavStatus.SC_NOT_FOUND ) );

                    generatedXML.writeElement( null, "propstat", XMLWriter.OPENING );
                    generatedXML.writeElement( null, "prop", XMLWriter.OPENING );

                    Iterator notFoundIter = propertiesNotFound.iterator();
                    while ( notFoundIter.hasNext() )
                    {
                        generatedXML.writeElement( null, (String) notFoundIter.next(), XMLWriter.NO_CONTENT );
                    }

                    generatedXML.writeElement( null, "prop", XMLWriter.CLOSING );
                    generatedXML.writeElement( null, "status", XMLWriter.OPENING );
                    generatedXML.writeText( status );
                    generatedXML.writeElement( null, "status", XMLWriter.CLOSING );
                    generatedXML.writeElement( null, "propstat", XMLWriter.CLOSING );

                }

                break;

        }

        generatedXML.writeElement( null, "response", XMLWriter.CLOSING );
    }

    /**
     * Print the lock discovery information associated with a path.
     * 
     * @param path Path
     * @param generatedXML XML data to which the locks info will be appended
     * @return true if at least one lock was displayed
     */
    private boolean generateLockDiscovery( String path, XMLWriter generatedXML )
    {
        boolean wroteStart = false;

        LockInfo resourceLock = (LockInfo) resourceLocks.get( path );
        if ( resourceLock != null )
        {
            wroteStart = true;
            generatedXML.writeElement( null, "lockdiscovery", XMLWriter.OPENING );
            resourceLock.toXML( generatedXML );
        }

        Iterator iter = collectionLocks.iterator();
        while ( iter.hasNext() )
        {
            LockInfo currentLock = (LockInfo) iter.next();
            if ( path.startsWith( currentLock.path ) )
            {
                if ( !wroteStart )
                {
                    wroteStart = true;
                    generatedXML.writeElement( null, "lockdiscovery", XMLWriter.OPENING );
                }
                currentLock.toXML( generatedXML );
            }
        }

        if ( wroteStart )
        {
            generatedXML.writeElement( null, "lockdiscovery", XMLWriter.CLOSING );
        }
        else
        {
            return false;
        }

        return true;

    }
}
