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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;

import org.sonatype.webdav.MethodExecutionContext;
import org.sonatype.webdav.ResourceCollection;
import org.sonatype.webdav.ResourceException;
import org.sonatype.webdav.UnauthorizedException;
import org.sonatype.webdav.WebdavStatus;
import org.sonatype.webdav.util.DOMWriter;
import org.sonatype.webdav.util.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * Webdav LOCK method.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @plexus.component role="org.sonatype.webdav.Method" role-hint="lock"
 * @since 1.0
 */
public class Lock
    extends AbstractWebdavMethod
{

    public void execute( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse res )
        throws IOException,
            UnauthorizedException
    {
        if ( !authorizeWrite( context.getUser(), res ) )
        {
            return;
        }

        if ( isReadOnly() )
        {
            res.sendError( WebdavStatus.SC_FORBIDDEN );
            return;
        }

        if ( isLocked( req ) )
        {
            res.sendError( WebdavStatus.SC_LOCKED );
            return;
        }

        LockInfo lock = new LockInfo();

        // Parsing lock request

        // Parsing depth header

        String depthStr = req.getHeader( "Depth" );

        if ( depthStr == null )
        {
            lock.depth = INFINITY;
        }
        else
        {
            if ( depthStr.equals( "0" ) )
            {
                lock.depth = 0;
            }
            else
            {
                lock.depth = INFINITY;
            }
        }

        // Parsing timeout header

        int lockDuration = DEFAULT_TIMEOUT;
        String lockDurationStr = req.getHeader( "Timeout" );
        if ( lockDurationStr == null )
        {
            lockDuration = DEFAULT_TIMEOUT;
        }
        else
        {
            int commaPos = lockDurationStr.indexOf( "," );
            // If multiple timeouts, just use the first
            if ( commaPos != -1 )
            {
                lockDurationStr = lockDurationStr.substring( 0, commaPos );
            }
            if ( lockDurationStr.startsWith( "Second-" ) )
            {
                lockDuration = ( new Integer( lockDurationStr.substring( 7 ) ) ).intValue();
            }
            else
            {
                if ( lockDurationStr.equalsIgnoreCase( "infinity" ) )
                {
                    lockDuration = MAX_TIMEOUT;
                }
                else
                {
                    try
                    {
                        lockDuration = ( new Integer( lockDurationStr ) ).intValue();
                    }
                    catch ( NumberFormatException e )
                    {
                        lockDuration = MAX_TIMEOUT;
                    }
                }
            }
            if ( lockDuration == 0 )
            {
                lockDuration = DEFAULT_TIMEOUT;
            }
            if ( lockDuration > MAX_TIMEOUT )
            {
                lockDuration = MAX_TIMEOUT;
            }
        }
        lock.expiresAt = System.currentTimeMillis() + ( lockDuration * 1000 );

        int lockRequestType = LOCK_CREATION;

        Node lockInfoNode = null;

        DocumentBuilder documentBuilder = getDocumentBuilder();

        try
        {
            Document document = documentBuilder.parse( new InputSource( req.getInputStream() ) );

            // Get the root element of the document
            Element rootElement = document.getDocumentElement();
            lockInfoNode = rootElement;
        }
        catch ( Exception e )
        {
            lockRequestType = LOCK_REFRESH;
        }

        if ( lockInfoNode != null )
        {

            // Reading lock information

            NodeList childList = lockInfoNode.getChildNodes();
            StringWriter strWriter = null;
            DOMWriter domWriter = null;

            Node lockScopeNode = null;
            Node lockTypeNode = null;
            Node lockOwnerNode = null;

            for ( int i = 0; i < childList.getLength(); i++ )
            {
                Node currentNode = childList.item( i );
                switch ( currentNode.getNodeType() )
                {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        String nodeName = currentNode.getNodeName();
                        if ( nodeName.endsWith( "lockscope" ) )
                        {
                            lockScopeNode = currentNode;
                        }
                        if ( nodeName.endsWith( "locktype" ) )
                        {
                            lockTypeNode = currentNode;
                        }
                        if ( nodeName.endsWith( "owner" ) )
                        {
                            lockOwnerNode = currentNode;
                        }
                        break;
                }
            }

            if ( lockScopeNode != null )
            {

                childList = lockScopeNode.getChildNodes();
                for ( int i = 0; i < childList.getLength(); i++ )
                {
                    Node currentNode = childList.item( i );
                    switch ( currentNode.getNodeType() )
                    {
                        case Node.TEXT_NODE:
                            break;
                        case Node.ELEMENT_NODE:
                            String tempScope = currentNode.getNodeName();
                            if ( tempScope.indexOf( ':' ) != -1 )
                            {
                                lock.scope = tempScope.substring( tempScope.indexOf( ':' ) + 1 );
                            }
                            else
                            {
                                lock.scope = tempScope;
                            }
                            break;
                    }
                }

                if ( lock.scope == null )
                {
                    // Bad request
                    res.setStatus( WebdavStatus.SC_BAD_REQUEST );
                }

            }
            else
            {
                // Bad request
                res.setStatus( WebdavStatus.SC_BAD_REQUEST );
            }

            if ( lockTypeNode != null )
            {

                childList = lockTypeNode.getChildNodes();
                for ( int i = 0; i < childList.getLength(); i++ )
                {
                    Node currentNode = childList.item( i );
                    switch ( currentNode.getNodeType() )
                    {
                        case Node.TEXT_NODE:
                            break;
                        case Node.ELEMENT_NODE:
                            String tempType = currentNode.getNodeName();
                            if ( tempType.indexOf( ':' ) != -1 )
                            {
                                lock.type = tempType.substring( tempType.indexOf( ':' ) + 1 );
                            }
                            else
                            {
                                lock.type = tempType;
                            }
                            break;
                    }
                }

                if ( lock.type == null )
                {
                    // Bad request
                    res.setStatus( WebdavStatus.SC_BAD_REQUEST );
                }

            }
            else
            {
                // Bad request
                res.setStatus( WebdavStatus.SC_BAD_REQUEST );
            }

            if ( lockOwnerNode != null )
            {

                childList = lockOwnerNode.getChildNodes();
                for ( int i = 0; i < childList.getLength(); i++ )
                {
                    Node currentNode = childList.item( i );
                    switch ( currentNode.getNodeType() )
                    {
                        case Node.TEXT_NODE:
                            lock.owner += currentNode.getNodeValue();
                            break;
                        case Node.ELEMENT_NODE:
                            strWriter = new StringWriter();
                            domWriter = new DOMWriter( strWriter, true );
                            domWriter.setQualifiedNames( false );
                            domWriter.print( currentNode );
                            lock.owner += strWriter.toString();
                            break;
                    }
                }

                if ( lock.owner == null )
                {
                    // Bad request
                    res.setStatus( WebdavStatus.SC_BAD_REQUEST );
                }

            }
            else
            {
                lock.owner = new String();
            }

        }

        String path = getRelativePath( req );

        lock.path = path;

        boolean exists = true;
        Object object = null;
        try
        {
            object = resourceCollection.lookup( context, path );
        }
        catch ( ResourceException e )
        {
            exists = false;
        }

        if ( lockRequestType == LOCK_CREATION )
        {

            // Generating lock id
            String lockTokenStr = req.getServletPath() + "-" + lock.type + "-" + lock.scope + "-"
                + req.getUserPrincipal() + "-" + lock.depth + "-" + lock.owner + "-" + lock.tokens + "-"
                + lock.expiresAt + "-" + System.currentTimeMillis() + "-" + secret;
            String lockToken = md5Encoder.encode( md5Helper.digest( lockTokenStr.getBytes() ) );

            if ( ( exists ) && ( object instanceof ResourceCollection ) && ( lock.depth == INFINITY ) )
            {

                // Locking a collection (and all its member resourceCollection)

                // Checking if a child resource of this collection is
                // already locked
                List lockPaths = new Vector();

                Iterator iter = collectionLocks.iterator();
                while ( iter.hasNext() )
                {
                    LockInfo currentLock = (LockInfo) iter.next();
                    if ( currentLock.hasExpired() )
                    {
                        resourceLocks.remove( currentLock.path );
                        continue;
                    }
                    if ( ( currentLock.path.startsWith( lock.path ) )
                        && ( ( currentLock.isExclusive() ) || ( lock.isExclusive() ) ) )
                    {
                        // A child collection of this collection is locked
                        lockPaths.add( currentLock.path );
                    }
                }

                // Yikes: modifying the collection not by the iterator's object,
                // but by one of its attributes. That means we can't use a
                // normal java.util.Iterator here, because Iterator is fail-fast. ;(
                Enumeration locksList = Collections.enumeration( resourceLocks.values() );
                while ( locksList.hasMoreElements() )
                {
                    LockInfo currentLock = (LockInfo) locksList.nextElement();
                    if ( currentLock.hasExpired() )
                    {
                        resourceLocks.remove( currentLock.path );
                        continue;
                    }
                    if ( ( currentLock.path.startsWith( lock.path ) )
                        && ( ( currentLock.isExclusive() ) || ( lock.isExclusive() ) ) )
                    {
                        // A child resource of this collection is locked
                        lockPaths.add( currentLock.path );
                    }
                }

                if ( !lockPaths.isEmpty() )
                {
                    // One of the child paths was locked
                    // We generate a multistatus error report
                    res.setStatus( WebdavStatus.SC_CONFLICT );

                    XMLWriter generatedXML = new XMLWriter();
                    generatedXML.writeXMLHeader();

                    generatedXML
                        .writeElement( null, "multistatus" + generateNamespaceDeclarations(), XMLWriter.OPENING );

                    iter = lockPaths.iterator();
                    while ( iter.hasNext() )
                    {
                        generatedXML.writeElement( null, "response", XMLWriter.OPENING );
                        generatedXML.writeElement( null, "href", XMLWriter.OPENING );
                        generatedXML.writeText( (String) iter.next() );
                        generatedXML.writeElement( null, "href", XMLWriter.CLOSING );
                        generatedXML.writeElement( null, "status", XMLWriter.OPENING );
                        generatedXML.writeText( "HTTP/1.1 " + WebdavStatus.SC_LOCKED + " "
                            + WebdavStatus.getStatusText( WebdavStatus.SC_LOCKED ) );
                        generatedXML.writeElement( null, "status", XMLWriter.CLOSING );

                        generatedXML.writeElement( null, "response", XMLWriter.CLOSING );
                    }

                    generatedXML.writeElement( null, "multistatus", XMLWriter.CLOSING );

                    Writer writer = res.getWriter();
                    writer.write( generatedXML.toString() );
                    writer.close();

                    return;

                }

                boolean addLock = true;

                // Checking if there is already a shared lock on this path
                iter = collectionLocks.iterator();
                while ( iter.hasNext() )
                {
                    LockInfo currentLock = (LockInfo) iter.next();
                    if ( currentLock.path.equals( lock.path ) )
                    {
                        if ( currentLock.isExclusive() )
                        {
                            res.sendError( WebdavStatus.SC_LOCKED );
                            return;
                        }
                        else
                        {
                            if ( lock.isExclusive() )
                            {
                                res.sendError( WebdavStatus.SC_LOCKED );
                                return;
                            }
                        }

                        currentLock.tokens.add( lockToken );
                        lock = currentLock;
                        addLock = false;
                    }
                }

                if ( addLock )
                {
                    lock.tokens.add( lockToken );
                    collectionLocks.add( lock );

                    // Add the Lock-Token header as by RFC 2518 8.10.1
                    // - only do this for newly created locks
                    res.addHeader( "Lock-Token", "<opaquelocktoken:" + lockToken + ">" );

                }

            }
            else
            {

                // Locking a single resource

                // Retrieving an already existing lock on that resource
                LockInfo presentLock = (LockInfo) resourceLocks.get( lock.path );
                if ( presentLock != null )
                {

                    if ( ( presentLock.isExclusive() ) || ( lock.isExclusive() ) )
                    {
                        // If either lock is exclusive, the lock can't be
                        // granted
                        res.sendError( WebdavStatus.SC_PRECONDITION_FAILED );
                        return;
                    }
                    else
                    {
                        presentLock.tokens.add( lockToken );
                        lock = presentLock;
                    }

                }
                else
                {

                    lock.tokens.add( lockToken );
                    resourceLocks.put( lock.path, lock );

                    // Checking if a resource exists at this path
                    exists = true;
                    try
                    {
                        object = resourceCollection.lookup( context, path );
                    }
                    catch ( ResourceException e )
                    {
                        exists = false;
                    }
                    if ( !exists )
                    {

                        // "Creating" a lock-null resource
                        int slash = lock.path.lastIndexOf( '/' );
                        String parentPath = lock.path.substring( 0, slash );

                        List lockNulls = (List) lockNullResources.get( parentPath );
                        if ( lockNulls == null )
                        {
                            lockNulls = new Vector();
                            lockNullResources.put( parentPath, lockNulls );
                        }

                        lockNulls.add( lock.path );

                    }
                    // Add the Lock-Token header as by RFC 2518 8.10.1
                    // - only do this for newly created locks
                    res.addHeader( "Lock-Token", "<opaquelocktoken:" + lockToken + ">" );
                }

            }

        }

        if ( lockRequestType == LOCK_REFRESH )
        {

            String ifHeader = req.getHeader( "If" );
            if ( ifHeader == null )
            {
                ifHeader = "";
            }

            // Checking resource locks
            LockInfo toRenew = (LockInfo) resourceLocks.get( path );
            if ( toRenew != null )
            {
                // At least one of the tokens of the locks must have been given
                Iterator iter = toRenew.tokens.iterator();
                while ( iter.hasNext() )
                {
                    String token = (String) iter.next();
                    if ( ifHeader.indexOf( token ) != -1 )
                    {
                        toRenew.expiresAt = lock.expiresAt;
                        lock = toRenew;
                    }
                }

            }

            // Checking inheritable collection locks
            Iterator iter = collectionLocks.iterator();
            while ( iter.hasNext() )
            {
                toRenew = (LockInfo) iter.next();
                if ( path.equals( toRenew.path ) )
                {
                    Iterator tokenIter = toRenew.tokens.iterator();
                    while ( tokenIter.hasNext() )
                    {
                        String token = (String) tokenIter.next();
                        if ( ifHeader.indexOf( token ) != -1 )
                        {
                            toRenew.expiresAt = lock.expiresAt;
                            lock = toRenew;
                        }
                    }

                }
            }

        }

        // Set the status, then generate the XML response containing
        // the lock information
        XMLWriter generatedXML = new XMLWriter();
        generatedXML.writeXMLHeader();
        generatedXML.writeElement( null, "prop" + generateNamespaceDeclarations(), XMLWriter.OPENING );

        generatedXML.writeElement( null, "lockdiscovery", XMLWriter.OPENING );

        lock.toXML( generatedXML );

        generatedXML.writeElement( null, "lockdiscovery", XMLWriter.CLOSING );

        generatedXML.writeElement( null, "prop", XMLWriter.CLOSING );

        res.setStatus( WebdavStatus.SC_OK );
        res.setContentType( "text/xml; charset=UTF-8" );
        Writer writer = res.getWriter();
        writer.write( generatedXML.toString() );
        writer.close();

    }
}
