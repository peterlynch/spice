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
package org.sonatype.plexus.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.collections.functors.TruePredicate;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.context.ContextException;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.data.MediaType;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.util.Template;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;
import org.sonatype.plexus.rest.xstream.json.PrimitiveKeyedMapConverter;
import org.sonatype.plexus.rest.xstream.xml.LookAheadXppDriver;

import com.noelios.restlet.application.Encoder;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;

/**
 * An abstract Restlet.org application, that should be extended for custom application needs. It will automatically pick
 * up existing PlexusResources, but is also able to take the "old way" for creating application root. Supports out of
 * the box JSON and XML representations powered by XStream, and also offers help in File Upload handling.
 * 
 * @author cstamas
 */
@Component( role = Application.class )
public class PlexusRestletApplicationBridge
    extends WadlApplication
{
    /** Key to store JSON driver driven XStream */
    public static final String JSON_XSTREAM = "plexus.xstream.json";

    /** Key to store XML driver driven XStream */
    public static final String XML_XSTREAM = "plexus.xstream.xml";

    /** Key to store used Commons Fileupload FileItemFactory */
    public static final String FILEITEM_FACTORY = "plexus.fileItemFactory";

    /** Key to store the flag should plexus discover resource or no */
    public static final String PLEXUS_DISCOVER_RESOURCES = "plexus.discoverResources";

    private static final String ENABLE_ENCODER_KEY = "enable-restlet-encoder";
    
    @Requirement
    private PlexusContainer plexusContainer;

    @Requirement( role = PlexusResource.class )
    private Map<String, PlexusResource> plexusResources;

    /** Date of creation of this application */
    private final Date createdOn;

    /** The root that is changeable as-needed basis */
    private RetargetableRestlet root;

    /** The root */
    private Router rootRouter;

    /** The applicationRouter */
    private Router applicationRouter;

    /**
     * Constructor.
     */
    public PlexusRestletApplicationBridge()
    {
        this.createdOn = new Date();

        setAutoDescribed( true );
    }

    /**
     * Constructor.
     * 
     * @param context
     */
    public PlexusRestletApplicationBridge( Context context )
    {
        super( context );

        this.createdOn = new Date();

        setAutoDescribed( true );
    }

    /**
     * Gets you the plexus container.
     * 
     * @return
     */
    protected PlexusContainer getPlexusContainer()
    {
        return plexusContainer;
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

    /**
     * Invoked from restlet.org Application once, to create root.
     */
    public final Restlet createRoot()
    {
        if ( root == null )
        {
            root = new RetargetableRestlet( getContext() );
        }

        configure();

        recreateRoot( true );

        // cheat, to avoid endless loop
        setRoot( root );

        afterCreateRoot( root );

        return root;
    }

    protected void afterCreateRoot( RetargetableRestlet root )
    {
        // empty
    }

    protected Router getRootRouter()
    {
        return rootRouter;
    }

    protected Router getApplicationRouter()
    {
        return applicationRouter;
    }

    /**
     * Creating all sort of shared tools and putting them into context, to make them usable by per-request
     * instantaniated Resource implementors.
     */
    protected final void configure()
    {
        // sorting out the resources, collecting them
        boolean shouldCollectPlexusResources =
            getContext().getParameters().getFirstValue( PLEXUS_DISCOVER_RESOURCES ) != null ? Boolean
                .parseBoolean( (String) getContext().getParameters().getFirstValue( PLEXUS_DISCOVER_RESOURCES ) )
                            : true; // the default if not set

        if ( shouldCollectPlexusResources )
        {
            // discover the plexusResources
            getLogger().info( "Discovered " + plexusResources.size() + " PlexusResource components." );
        }
        else
        {
            // create an empty map
            plexusResources = new HashMap<String, PlexusResource>();

            getLogger().info( "PlexusResource discovery disabled." );
        }

        // we are putting XStream into this Application's Context, since XStream is threadsafe
        // and it is safe to share it across multiple threads. XStream is heavily used by our
        // custom Representation implementation to support XML and JSON.

        // create and configure XStream for JSON
        XStream xstream = createAndConfigureXstream( new JsonOrgHierarchicalStreamDriver() );

        // for JSON, we use a custom converter for Maps
        xstream.registerConverter( new PrimitiveKeyedMapConverter( xstream.getMapper() ) );

        // put it into context
        getContext().getAttributes().put( JSON_XSTREAM, xstream );

        // create and configure XStream for XML
        xstream = createAndConfigureXstream( new LookAheadXppDriver() );

        // put it into context
        getContext().getAttributes().put( XML_XSTREAM, xstream );

        // put fileItemFactory into context
        getContext().getAttributes().put( FILEITEM_FACTORY, new DiskFileItemFactory() );

        doConfigure();
    }

    protected final void recreateRoot( boolean isStarted )
    {
        // reboot?
        if ( root != null )
        {
            // create a new root router
            rootRouter = new Router( getContext() );

            applicationRouter = initializeRouter( rootRouter, isStarted );

            // attach all PlexusResources
            if ( isStarted )
            {
                for ( PlexusResource resource : plexusResources.values() )
                {
                    attach( applicationRouter, false, resource );
                }
            }

            doCreateRoot( rootRouter, isStarted );
            
            // check if we want to compress stuff
            boolean enableCompression = false;
            try
            {
                if( this.plexusContainer.getContext().contains( ENABLE_ENCODER_KEY ) && 
                    Boolean.parseBoolean( this.plexusContainer.getContext().get( ENABLE_ENCODER_KEY ).toString() ) )
                {
                    enableCompression = true;
                    getLogger().fine( "Restlet Encoder will compress output." );
                }
            }
            catch ( ContextException e )
            {
                getLogger().log( Level.WARNING, "Failed to get plexus property: "+ ENABLE_ENCODER_KEY + ", this property was found in the context.", e );
            }
            
            // encoding support
            ArrayList<MediaType> ignoredMediaTypes = new ArrayList<MediaType>(Encoder.getDefaultIgnoredMediaTypes());
            ignoredMediaTypes.add( MediaType.APPLICATION_COMPRESS ); // anything compressed
            ignoredMediaTypes.add( new MediaType( "application/x-bzip2" ) );
            ignoredMediaTypes.add( new MediaType( "application/x-bzip" ) );
            ignoredMediaTypes.add( new MediaType( "application/x-compressed" ) );
            ignoredMediaTypes.add( new MediaType( "application/x-shockwave-flash" ) );
            
            Encoder encoder = new Encoder( getContext(), false, enableCompression, Encoder.ENCODE_ALL_SIZES,
                Encoder.getDefaultAcceptedMediaTypes(), ignoredMediaTypes);
            
            encoder.setNext( rootRouter );
            
            // set it
            root.setNext( encoder );
            
        }
    }
    
    

    protected final XStream createAndConfigureXstream( HierarchicalStreamDriver driver )
    {
        XStream xstream = new XStream( driver );

        xstream.setClassLoader( new WholeWorldClassloader( getPlexusContainer().getContainerRealm().getWorld() ) );

        // let the application configure the XStream
        xstream = doConfigureXstream( xstream );

        // then apply all the needed stuff from Resources
        for ( PlexusResource resource : plexusResources.values() )
        {
            resource.configureXStream( xstream );
        }

        // and return it
        return xstream;
    }

    protected void attach( Router router, boolean strict, String uriPattern, Restlet target )
    {
        if ( getLogger().isLoggable( Level.FINE ) )
        {
            getLogger().log(
                             Level.FINE,
                             "Attaching Restlet of class '" + target.getClass().getName() + "' to URI='" + uriPattern
                                 + "' (strict='" + strict + "')" );
        }

        Route route = router.attach( uriPattern, target );

        if ( strict )
        {
            route.getTemplate().setMatchingMode( Template.MODE_EQUALS );
        }
    }

    protected void attach( Router router, boolean strict, PlexusResource resource )
    {
        attach( router, strict, resource.getResourceUri(), new PlexusResourceFinder( getContext(), resource ) );

        handlePlexusResourceSecurity( resource );
    }

    protected void handlePlexusResourceSecurity( PlexusResource resource )
    {
        // empty default imple
    }

    // methods to override

    /**
     * Method to be overridden by subclasses. It will be called only once in the lifetime of this Application. This is
     * the place when you need to create and add to context some stuff.
     */
    protected void doConfigure()
    {
        // empty implementation, left for subclasses to do something meaningful
    }

    /**
     * Method to be overridden by subclasses. It will be called multiple times with multiple instances of XStream.
     * Configure it by adding aliases for used DTOs, etc.
     * 
     * @param xstream
     * @return
     */
    public XStream doConfigureXstream( XStream xstream )
    {
        // default implementation does nothing, override if needed
        return xstream;
    }

    /**
     * Left for subclass to inject a "prefix" path. The automatically managed PlexusResources will be attached under the
     * router returned by this method.
     * 
     * @param root
     * @return
     */
    protected Router initializeRouter( Router root, boolean isStarted )
    {
        return root;
    }

    /**
     * Called when the app root needs to be created. Override it if you need "old way" to attach resources, or need to
     * use the isStarted flag.
     * 
     * @param root
     * @param isStarted
     */
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        // empty implementation, left for subclasses to do something meaningful
    }

}
