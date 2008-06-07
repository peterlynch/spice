package org.sonatype.webdav;

import java.util.Enumeration;

/**
 * @author Jason van Zyl
 */
public interface ResourceCollection
{
    public Enumeration<Object> list( MethodExecutionContext context, String name )
        throws ResourceException,
            UnauthorizedException;

    public Object lookup( MethodExecutionContext context, String key )
        throws ResourceException,
            UnauthorizedException;

    public void add( MethodExecutionContext context, String path, Resource resource )
        throws ResourceException,
            UnauthorizedException;

    public void delete( MethodExecutionContext context, String path )
        throws ResourceException,
            UnauthorizedException;

    public void replace( MethodExecutionContext context, String path, Resource resource )
        throws ResourceException,
            UnauthorizedException;

    public void createSubcontext( MethodExecutionContext context, String path )
        throws ResourceException,
            UnauthorizedException;

    public Resource createResource( MethodExecutionContext context, String deepPath );

    public String getPath();

    public long getLastModified();

    public String getLastModifiedHttp();

    public long getCreation();
}
