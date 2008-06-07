package org.sonatype.webdav;

import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Andrew Williams
 * @plexus.component role="org.sonatype.webdav.ResourceCollection" role-hint="memory"
 */
public class MemoryResourceCollection
    extends AbstractResourceCollection
{
    Vector<Object> resources = new Vector<Object>();

    private long created = System.currentTimeMillis();

    public MemoryResourceCollection()
    {
        this( "/" );
    }

    public MemoryResourceCollection( String id )
    {
        super( id );
    }

    public Enumeration<Object> listResources( MethodExecutionContext context )
    {
        return resources.elements();
    }

    public void addResource( MethodExecutionContext context, Resource resource )
    {
        resources.add( resource );
    }

    public void removeResource( MethodExecutionContext context, Resource resource )
    {
        resources.remove( resource );
        ( (MemoryResource) resource ).remove( context );
    }

    public void replaceResource( MethodExecutionContext context, Resource old, Resource resource )
    {
        resources.remove( old );
        resources.add( resource );
    }

    public ResourceCollection createCollection( MethodExecutionContext context, String path )
    {
        ResourceCollection ret = new MemoryResourceCollection( path );

        resources.add( ret );
        return ret;
    }

    public void removeCollection( MethodExecutionContext context, ResourceCollection collection )
    {
        resources.remove( collection );
    }

    public Resource createResource( MethodExecutionContext context, String deepPath )
    {
        return new MemoryResource();
    }

    public long getLastModified()
    {
        long modified = created;

        Enumeration<Object> res = resources.elements();
        while ( res.hasMoreElements() )
        {
            Object o = res.nextElement();

            if ( o instanceof Resource )
            {
                modified = Math.max( created, ( (Resource) o ).getCreation() );
            }
        }

        return modified;
    }

    public long getCreation()
    {
        return created;
    }
}
