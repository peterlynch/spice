/**
 * Copyright (C) 2008 Sonatype Inc. 
 * Sonatype Inc, licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
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
package org.sonatype.plexus.rest;

import java.util.Date;
import java.util.Map;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;
import org.sonatype.plexus.rest.xstream.json.PrimitiveKeyedMapConverter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * An abstract Restlet.org application, that should be extended for custom application needs. It will automatically pick
 * up existing PlexusResources, but is also able to take the "old way" for creating application root. Supports out of
 * the box JSON and XML representations powered by XStream, and also offers help in File Upload handling.
 * 
 * @author cstamas
 */
public class PlexusRestletApplicationBridge
    extends Application
{
    /** Key to store JSON driver driven XStream */
    public static final String JSON_XSTREAM = "xstream.json";

    /** Key to store XML driver driven XStream */
    public static final String XML_XSTREAM = "xstream.xml";

    /** Key to store used Commons Fileupload FileItemFactory */
    public static final String FILEITEM_FACTORY = "nexus.fileItemFactory";

    /** Date of creation of this application */
    private final Date createdOn;

    /** The root that is changeable as-needed basis */
    private RetargetableRestlet root;

    private Map<String, PlexusResource> plexusResources;

    public PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getContext().getAttributes().get( PlexusConstants.PLEXUS_KEY );
    }

    public void setPlexusContainer( PlexusContainer plexusContainer )
    {
        getContext().getAttributes().put( PlexusConstants.PLEXUS_KEY, plexusContainer );
    }

    public PlexusRestletApplicationBridge( Context context )
    {
        super( context );

        this.createdOn = new Date();
    }

    /**
     * Creating all sort of shared tools and putting them into context, to make them usable by per-request
     * instantaniated Resource implementors.
     */
    protected void configure()
    {
        // we are putting XStream into this Application's Context, since XStream is threadsafe
        // and it is safe to share it across multiple threads. XStream is heavily used by our
        // custom Representation implementation to support XML and JSON.

        // create and configure XStream for JSON
        XStream xstream = configureXstream( new XStream( new JsonOrgHierarchicalStreamDriver() ) );

        // for JSON, we use a custom converter for Maps
        xstream.registerConverter( new PrimitiveKeyedMapConverter( xstream.getMapper() ) );

        // put it into context
        getContext().getAttributes().put( JSON_XSTREAM, xstream );

        // create and configure XStream for XML
        xstream = configureXstream( new XStream( new DomDriver() ) );

        // put it into context
        getContext().getAttributes().put( XML_XSTREAM, xstream );

        // put fileItemFactory into context
        getContext().getAttributes().put( FILEITEM_FACTORY, new DiskFileItemFactory() );

        // collect the plexusResources
        try
        {
            plexusResources = (Map<String, PlexusResource>) getPlexusContainer().lookupMap( PlexusResource.class );

            getLogger().info( "Discovered " + plexusResources.size() + " PlexusResource components." );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "Cannot collect PlexusResources!", e );
        }
    }

    /**
     * Returns the timestamp of instantaniation of this object. This is used as timestamp for transient objects when
     * they are still unchanged (not modified).
     * 
     * @return date
     */
    public Date getCreatedOn()
    {
        return createdOn;
    }

    public final Restlet createRoot()
    {
        if ( root == null )
        {
            root = new RetargetableRestlet( getContext() );
        }

        configure();

        recreateRoot( true );

        return root;
    }

    protected final void recreateRoot( boolean isStarted )
    {
        // reboot?
        if ( root != null )
        {
            // create a new root router
            Router rootRouter = new Router( getContext() );

            // attach all PlexusResources
            if ( isStarted )
            {
                for ( PlexusResource resource : plexusResources.values() )
                {
                    rootRouter.attach( resource.getResourceUri(), new PlexusResourceFinder( getContext(), resource ) );
                }
            }

            // allow "manual" resource attachment too
            doCreateRoot( rootRouter, isStarted );

            // set it
            root.setRoot( rootRouter );
        }
    }

    protected XStream configureXstream( XStream xstream )
    {
        // default implementation does nothing, override if needed
        return xstream;
    }

    protected void doCreateRoot( Router root, boolean isStarted )
    {
        // empty implementation, left for subclasses to do something meaningful
    }

}
