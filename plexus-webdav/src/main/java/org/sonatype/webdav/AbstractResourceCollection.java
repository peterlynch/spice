package org.sonatype.webdav;

import org.sonatype.webdav.util.RequestUtil;

import java.util.Date;
import java.util.Enumeration;


/**
 * @author Andrew Williams
 */
public abstract class AbstractResourceCollection
    implements ResourceCollection
{
    private String path;

    public AbstractResourceCollection()
    {
        this( "/" );
    }

    public AbstractResourceCollection( String path )
    {
        if ( path.endsWith( "/" ) )
        {
            this.path = path;
        }
        else
        {
            this.path = path + "/";
        }
    }

    public String getPath()
    {
        return path;
    }

    public abstract Enumeration<Object> listResources( MethodExecutionContext context )
        throws ResourceException,
            UnauthorizedException;

    public Enumeration<Object> list( MethodExecutionContext context, String path )
        throws ResourceException,
            UnauthorizedException
    {
        Object resource = lookup( context, path );

        if ( resource instanceof ResourceCollection )
        {
            return ( (AbstractResourceCollection) resource ).listResources( context );
        }
        else
        {
            throw new ResourceException( "path " + path + " is not a collection" );
        }
    }

    public Object lookup( MethodExecutionContext context, String name )
        throws ResourceException,
            UnauthorizedException
    {
        if ( name.length() > 0 && name.charAt( 0 ) == '/' )
        {
            name = name.substring( 1 );
        }

        if ( name.equals( "" ) )
        {
            return this;
        }

        int pos = name.indexOf( "/" );
        if ( pos > -1 )
        {
            Object resource = lookup( context, name.substring( 0, pos ) );

            if ( resource instanceof ResourceCollection )
            {
                return ( (ResourceCollection) resource ).lookup( context, name.substring( pos + 1 ) );
            }
            else
            {
                throw new ResourceException( "path " + name.substring( 0, pos ) + " is not a collection" );
            }
        }

        Enumeration<Object> iter = listResources( context );
        while ( iter.hasMoreElements() )
        {
            Object resource = iter.nextElement();

            if ( ( resource instanceof Resource ) && name.equals( ( (Resource) resource ).getName() ) )
            {
                return resource;
            }

            if ( resource instanceof ResourceCollection )
            {
                String path = ( (ResourceCollection) resource ).getPath();
                String testPath = getPath() + name;

                if ( path.equals( testPath ) || path.equals( testPath + "/" ) )
                {
                    return resource;
                }
            }
        }

        throw new ResourceException( "Resource not found: " + name );
    }

    public void add( MethodExecutionContext context, String path, Resource resource )
        throws ResourceException,
            UnauthorizedException
    {
        ResourceCollection collection = getCollectionFromPath( context, path, true );
        String name = getNameFromPath( path );

        resource.setName( name );

        ( (AbstractResourceCollection) collection ).addResource( context, resource );
    }

    public abstract void addResource( MethodExecutionContext context, Resource resource )
        throws ResourceException,
            UnauthorizedException;

    public void delete( MethodExecutionContext context, String path )
        throws ResourceException,
            UnauthorizedException
    {
        ResourceCollection collection = getCollectionFromPath( context, path.substring( 0, path.length() - 1 ), false );

        Object old = lookup( context, path );
        if ( old instanceof ResourceCollection )
        {
            ( (AbstractResourceCollection) collection ).removeCollection( context, (ResourceCollection) old );
        }
        else
        {
            ( (AbstractResourceCollection) collection ).removeResource( context, (Resource) old );
        }
    }

    public abstract void removeResource( MethodExecutionContext context, Resource resource )
        throws ResourceException,
            UnauthorizedException;

    public abstract void removeCollection( MethodExecutionContext context, ResourceCollection collection )
        throws ResourceException,
            UnauthorizedException;

    public void replace( MethodExecutionContext context, String path, Resource resource )
        throws ResourceException,
            UnauthorizedException
    {
        ResourceCollection collection = getCollectionFromPath( context, path, false );
        String name = getNameFromPath( path );

        Resource old = (Resource) lookup( context, path );
        resource.setName( name );

        ( (AbstractResourceCollection) collection ).replaceResource( context, old, resource );
    }

    public abstract void replaceResource( MethodExecutionContext context, Resource old, Resource resource )
        throws ResourceException,
            UnauthorizedException;

    public abstract ResourceCollection createCollection( MethodExecutionContext context, String path )
        throws ResourceException,
            UnauthorizedException;

    public void createSubcontext( MethodExecutionContext context, String path )
        throws ResourceException,
            UnauthorizedException
    {
        ResourceCollection collection = getCollectionFromPath( context, path.substring( 0, path.length() - 1 ), true );

        ( (AbstractResourceCollection) collection ).createCollection( context, path );
    }

    public String getLastModifiedHttp()
    {
        return RequestUtil.formatHttpDate( new Date( getLastModified() ) );
    }

    private ResourceCollection getCollectionFromPath( MethodExecutionContext context, String path,
        boolean createIfNeeded )
        throws ResourceException,
            UnauthorizedException
    {
        int pos = path.indexOf( "/" );

        if ( pos > -1 )
        {
            Object resource = null;
            try
            {
                resource = lookup( context, path.substring( 0, pos ) );
            }
            catch ( ResourceException e )
            {
                if ( createIfNeeded )
                {
                    resource = createCollection( context, path.substring( 0, pos ) );
                }
                else
                {
                    throw e;
                }
            }

            if ( resource instanceof ResourceCollection )
            {
                return ( (AbstractResourceCollection) resource ).getCollectionFromPath( context, path
                    .substring( pos + 1 ), createIfNeeded );
            }
            else
            {
                throw new ResourceException( "path " + path.substring( 0, pos ) + " is not a collection" );
            }
        }

        return this;
    }

    private String getNameFromPath( String path )
    {
        int pos = path.lastIndexOf( "/" );

        return path.substring( pos + 1 );
    }
}
