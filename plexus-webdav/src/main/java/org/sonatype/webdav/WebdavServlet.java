/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.webdav;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.webdav.security.Authentication;
import org.sonatype.webdav.security.Authorization;
import org.sonatype.webdav.security.User;


// TODO:
// turn into a server
// abstract the protocol from the underlying network operations (i.e using MINA or Grizzly should be simple)
// use xml pull parser for xml processing
// use plexus org.sonatype.webdav.naming
// handler for each protocol method

/**
 * Servlet which adds support for WebDAV level 2. All the basic HTTP requests are handled by the DefaultServlet. <p/>
 * Check out http://issues.apache.org/bugzilla/show_bug.cgi?id=40160 for a Filter that allows you to map this servlet
 * anywhere inside a web application, not just the /* URL pattern.
 * 
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public class WebdavServlet
    extends HttpServlet
{

    /**
     * The debugging detail level for this servlet.
     */
    protected int debug = 9;

    /**
     * Should we generate directory listings?
     */
    protected boolean listings = true;

    /**
     * Read only flag. By default, it's set to true.
     */
    protected boolean readOnly = false;

    /**
     * Proxy directory context.
     */
    protected ResourceCollection resourceCollection;

    protected String resourceCollectionHint = null;

    /**
     * File encoding to be used when reading static files. If none is specified the platform default is used.
     */
    protected String fileEncoding = null;

    private String secret = null;

    /**
     * Our authentication
     */
    protected Authentication authn;

    protected Authorization authz;

    protected String authenticationHint;

    protected String authorizationHint;

    
    protected String getConfigParameter( String name )
    {
        Object o = getServletContext().getAttribute( name );
        
        return o == null ? null : o.toString();
            
    }
    /**
     * Initialize this servlet.
     */
    public void init()
        throws ServletException
    {

        super.init();

        PlexusContainer container = getContainer( getServletContext() );

        if ( container == null )
        {
            throw new ServletException( "Cannot find PlexusContainer in the webapp context" );
        }

        // Set our properties from the initialization parameters
        String value = null;
        try
        {
            value = getConfigParameter( "debug" );
            if ( value != null )
            {
                debug = Integer.parseInt( value );
            }
        }
        catch ( Exception e )
        {
            log( "DefaultServlet.init: couldn't read debug from " + value );
        }

        try
        {
            value = getConfigParameter( "listings" );
            if ( value != null )
            {
                listings = ( new Boolean( value ) ).booleanValue();
            }
        }
        catch ( Exception e )
        {
            log( "DefaultServlet.init: couldn't read listings from " + value );
        }

        try
        {
            value = getConfigParameter( "readonly" );
            if ( value != null )
            {
                readOnly = ( new Boolean( value ) ).booleanValue();
            }
        }
        catch ( Exception e )
        {
            log( "DefaultServlet.init: couldn't read readonly from " + value );
        }

        try
        {
            value = getConfigParameter( "fileEncoding" );
            if ( value != null )
            {
                fileEncoding = value;
            }
        }
        catch ( Exception e )
        {
            log( "DefaultServlet.init: couldn't read fileEncoding from " + value );
        }

        try
        {
            value = getConfigParameter( "secret" );
            if ( value != null )
            {
                secret = value;
            }
        }
        catch ( Exception e )
        {
            log( "WebdavServlet.init: error reading secret from " + value );
        }

        try
        {
            value = getConfigParameter( "resourceCollectionHint" );
            if ( value != null )
            {
                resourceCollectionHint = value;
            }
            else
            {
                resourceCollectionHint = "file";
            }
        }
        catch ( Exception e )
        {
            log( "WebdavServlet.init: error reading resource collection hint from " + value );
            resourceCollectionHint = "file";
        }

        try
        {
            value = getConfigParameter( "authenticationHint" );
            if ( value != null )
            {
                authenticationHint = value;
            }
            else
            {
                authenticationHint = "properties";
            }
        }
        catch ( Exception e )
        {
            log( "WebdavServlet.init: error reading secret from " + value );
            authenticationHint = "properties";
        }

        try
        {
            value = getConfigParameter( "authorizationHint" );
            if ( value != null )
            {
                authorizationHint = value;
            }
            else
            {
                authorizationHint = "properties";
            }
        }
        catch ( Exception e )
        {
            log( "WebdavServlet.init: error reading secret from " + value );
            authorizationHint = "properties";
        }

        try
        {
            authn = (Authentication) container.lookup( Authentication.class, authenticationHint );
            authz = (Authorization) container.lookup( Authorization.class, authorizationHint );
            resourceCollection = (ResourceCollection) container.lookup(
                ResourceCollection.class,
                resourceCollectionHint );
        }
        catch ( ComponentLookupException e )
        {
            throw new ServletException( "Not all components were available", e );
        }

    }

    /**
     * Handles the special WebDAV methods.
     */
    protected void service( HttpServletRequest req, HttpServletResponse res )
        throws ServletException,
            IOException
    {
        User user = authn.authenticate( req, res, req.getSession() );

        if ( user == null )
        {
            // our authentication implementation should be logging them in so we can render on the next request
            return;
        }

        String method = req.getMethod();

        if ( debug > 0 )
        {
            log( "[" + method + "] " + req.getPathInfo() );
        }

        Method methodExecution = loadMethod( method );
        if ( methodExecution == null )
        {
            res.sendError( HttpServletResponse.SC_NOT_IMPLEMENTED );
        }
        else
        {
            MethodExecutionContext context = new DefaultMethodExecutionContext( user, req, res );
            try
            {
                methodExecution.execute( context, req, res );
            }
            catch ( UnauthorizedException e )
            {
                res.reset();
                authn.challenge( user, req, res, req.getSession() );
                return;
            }
        }
    }

    private Method loadMethod( String method )
    {
        Method ret = null;

        try
        {
            ret = (Method) getContainer( getServletContext() ).lookup( Method.class, method.toLowerCase() );
        }
        catch ( ComponentLookupException e )
        {
            System.err.println( "WARNING: could not load method: " + method );
            e.printStackTrace();
        }

        if ( ret != null )
        {
            /* this could be a plexus requirement, but the lifecycle to choose is not straight forward */
            ret.setResourceCollection( resourceCollection );

            ret.setReadOnly( readOnly );
            ret.setDebug( debug );
            ret.setListings( listings );
            ret.setFileEncoding( fileEncoding );
            ret.setAuthorization( authz );

            if ( secret != null )
            {
                ret.setSecret( secret );
            }
        }

        return ret;
    }

    public static PlexusContainer getContainer( ServletContext context )
    {
        return (PlexusContainer) context.getAttribute( PlexusConstants.PLEXUS_KEY );
    }
}
