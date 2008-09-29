package org.sonatype.plexus.rest.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

/**
 * The delegating resource.
 * 
 * @author Jason van Zyl @author cstamas
 */
public class RestletResource
    extends Resource
{
    private PlexusResource delegate;

    public RestletResource( Context context, Request request, Response response, PlexusResource delegate )
    {
        super( context, request, response );

        this.delegate = delegate;

        // set variants
        getVariants().clear();
        getVariants().addAll( delegate.getVariants() );

        // mimic the constructor
        setAvailable( delegate.isAvailable() );
        setReadable( delegate.isReadable() );
        setModifiable( delegate.isModifiable() );
        setNegotiateContent( delegate.isNegotiateContent() );
    }

    private String getModificationDateKey( boolean parent )
    {
        if ( parent )
        {
            return getRequest().getResourceRef().getParentRef().getPath() + "#modified";
        }
        else
        {
            return getRequest().getResourceRef().getPath() + "#modified";
        }
    }

    protected Date getModificationDate()
    {
        Date result = (Date) getContext().getAttributes().get( getModificationDateKey( false ) );

        if ( result == null )
        {
            // get parent's date
            result = (Date) getContext().getAttributes().get( getModificationDateKey( true ) );

            if ( result == null )
            {
                // get app date
                PlexusRestletApplicationBridge application = (PlexusRestletApplicationBridge) getApplication();

                result = application.getCreatedOn();
            }

            getContext().getAttributes().put( getModificationDateKey( false ), result );
        }

        return result;
    }

    protected void updateModificationDate( boolean parent )
    {
        getContext().getAttributes().put( getModificationDateKey( parent ), new Date() );
    }

    /**
     * For file uploads we are using commons-fileupload integration with restlet.org. We are storing one FileItemFactory
     * instance in context. This method simply encapsulates gettting it from Resource context.
     * 
     * @return
     */
    protected FileItemFactory getFileItemFactory()
    {
        return (FileItemFactory) getContext().getAttributes().get( PlexusRestletApplicationBridge.FILEITEM_FACTORY );
    }

    protected XStreamRepresentation createRepresentation( Variant variant )
        throws ResourceException
    {
        XStreamRepresentation representation = null;

        try
        {
            String text = ( variant instanceof Representation ) ? ( (Representation) variant ).getText() : "";

            if ( MediaType.APPLICATION_JSON.equals( variant.getMediaType(), true ) )
            {
                representation = new XStreamRepresentation( (XStream) getContext().getAttributes().get(
                    PlexusRestletApplicationBridge.JSON_XSTREAM ), text, variant.getMediaType() );
            }
            else if ( MediaType.APPLICATION_XML.equals( variant.getMediaType(), true ) )
            {
                representation = new XStreamRepresentation( (XStream) getContext().getAttributes().get(
                    PlexusRestletApplicationBridge.XML_XSTREAM ), text, variant.getMediaType() );
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_ACCEPTABLE );
            }

            representation.setModificationDate( getModificationDate() );

            return representation;
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Cannot get the representation!", e );
        }
    }

    protected Representation serialize( Variant variant, Object payload )
        throws ResourceException
    {
        if ( payload == null )
        {
            return null;
        }

        XStreamRepresentation result = createRepresentation( variant );

        result.setPayload( payload );

        return result;
    }

    protected Object deserialize( Object root )
        throws ResourceException
    {
        XStreamRepresentation result = createRepresentation( getRequest().getEntity() );

        return result.getPayload( root );
    }

    protected Representation doRepresent( Object payload, Variant variant )
        throws ResourceException
    {
        if ( Representation.class.isAssignableFrom( payload.getClass() ) )
        {
            // representation
            return (Representation) payload;
        }
        else if ( InputStream.class.isAssignableFrom( payload.getClass() ) )
        {
            // inputStream
            return new InputStreamRepresentation( variant.getMediaType(), (InputStream) payload );
        }
        else if ( String.class.isAssignableFrom( payload.getClass() ) )
        {
            // inputStream
            return new StringRepresentation( (String) payload, variant.getMediaType() );
        }
        else
        {
            // object, make it a representation
            return serialize( variant, payload );
        }
    }

    public Representation represent( Variant variant )
        throws ResourceException
    {
        Object result = delegate.get( getContext(), getRequest(), getResponse(), variant );

        return doRepresent( result, variant );
    }

    public void acceptRepresentation( Representation representation )
        throws ResourceException
    {
        if ( delegate.acceptsUpload() )
        {
            upload( representation );
        }
        else
        {
            Object payload = deserialize( delegate.getPayloadInstance() );

            Object result = delegate.post( getContext(), getRequest(), getResponse(), payload );

            if ( result != null )
            {
                getResponse().setEntity( doRepresent( result, representation ) );
            }
        }

        if ( getResponse().getStatus().isSuccess() )
        {
            updateModificationDate( false );
        }
    }

    public void storeRepresentation( Representation representation )
        throws ResourceException
    {
        if ( delegate.acceptsUpload() )
        {
            upload( representation );
        }
        else
        {
            Object payload = deserialize( delegate.getPayloadInstance() );

            Object result = delegate.put( getContext(), getRequest(), getResponse(), payload );

            if ( result != null )
            {
                getResponse().setEntity( doRepresent( result, representation ) );
            }
        }

        if ( getResponse().getStatus().isSuccess() )
        {
            updateModificationDate( false );

            updateModificationDate( true );
        }
    }

    public void removeRepresentations()
        throws ResourceException
    {
        delegate.delete( getContext(), getRequest(), getResponse() );

        if ( getResponse().getStatus().isSuccess() )
        {
            updateModificationDate( false );

            updateModificationDate( true );
        }
    }

    public void upload( Representation representation )
        throws ResourceException
    {
        Object result = null;

        List<FileItem> files = null;

        try
        {
            RestletFileUpload uploadRequest = new RestletFileUpload( getFileItemFactory() );

            files = uploadRequest.parseRepresentation( representation );

            result = delegate.upload( getContext(), getRequest(), getResponse(), files );
        }
        catch ( FileUploadException e )
        {
            // try to take simply the body as stream
            String name = getRequest().getResourceRef().getPath();

            if ( name.contains( "/" ) )
            {
                name = name.substring( name.lastIndexOf( "/" ) + 1, name.length() );
            }

            FileItem file = new FakeFileItem( name, representation );

            files = new ArrayList<FileItem>();

            files.add( file );

            result = delegate.upload( getContext(), getRequest(), getResponse(), files );
        }

        if ( result != null )
        {
            getResponse().setEntity( doRepresent( result, representation ) );
        }
    }

    // ==

    private class FakeFileItem
        implements FileItem
    {
        private final String name;

        private final Representation representation;

        public FakeFileItem( String name, Representation representation )
        {
            this.name = name;

            this.representation = representation;
        }

        public String getContentType()
        {
            return representation.getMediaType().getName();
        }

        public String getName()
        {
            return name;
        }

        public String getFieldName()
        {
            return getName();
        }

        public InputStream getInputStream()
            throws IOException
        {
            return representation.getStream();
        }

        // == ignored methods

        public void delete()
        {
            // TODO Auto-generated method stub
        }

        public byte[] get()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public OutputStream getOutputStream()
            throws IOException
        {
            // TODO Auto-generated method stub
            return null;
        }

        public long getSize()
        {
            return 0;
        }

        public String getString()
        {
            return null;
        }

        public String getString( String encoding )
            throws UnsupportedEncodingException
        {
            return null;
        }

        public boolean isFormField()
        {
            return false;
        }

        public boolean isInMemory()
        {
            return false;
        }

        public void setFieldName( String name )
        {
        }

        public void setFormField( boolean state )
        {
        }

        public void write( File file )
            throws Exception
        {
        }

    }

}
