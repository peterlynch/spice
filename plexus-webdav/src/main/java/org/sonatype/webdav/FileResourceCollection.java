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

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Andrew Williams
 * @plexus.component role="org.sonatype.webdav.ResourceCollection" role-hint="file"
 */
public class FileResourceCollection
    extends AbstractResourceCollection
    implements Initializable
{
    private File resourcePath;
    
    public void initialize()
        throws InitializationException
    {
        if ( !resourcePath.exists() )
        {
            resourcePath.mkdirs();
        }
    }

    public FileResourceCollection()
    {
    }

    public FileResourceCollection( File resourcePath, String id )
    {
        super( id );
        
        this.resourcePath = resourcePath;
    }

    public Enumeration<Object> listResources( MethodExecutionContext context )
    {    	
        Vector<Object> ret = new Vector<Object>();

        File[] list = resourcePath.listFiles();
        
        if ( list != null )
        {
            for ( int i = 0; i < list.length; i++ )
            {
                File file = list[i];

                if ( file.isDirectory() )
                {
                    ret.add( new FileResourceCollection( file, getPath() + file.getName() + "/" ) );
                }
                else
                {
                    ret.add( new FileResource( file ) );
                }
            }
        }

        return ret.elements();
    }

    public void addResource( MethodExecutionContext context, Resource resource )
    {
        File underlying = ( (FileResource) resource ).getFile();

        File newName = new File( resourcePath, resource.getName() );

        move( underlying, newName );
        ( (FileResource) resource ).setFile( newName );
    }

    public void removeResource( MethodExecutionContext context, Resource resource )
    {
        ( (FileResource) resource ).remove( context );
    }

    public void replaceResource( MethodExecutionContext context, Resource old, Resource resource )
    {
        File underlying = ( (FileResource) old ).getFile();
        underlying.delete();

        File replace = new File( resourcePath, resource.getName() );

        move( ( (FileResource) resource ).getFile(), replace );
        ( (FileResource) resource ).setFile( replace );
    }

    public ResourceCollection createCollection( MethodExecutionContext context, String path )
    {
        File dir = new File( this.resourcePath, new File( path ).getName() );

        dir.mkdir();
        FileResourceCollection ret = new FileResourceCollection( dir, path );
        return ret;
    }

    public void removeCollection( MethodExecutionContext context, ResourceCollection collection )
    {
        File file = new File( resourcePath, new File( collection.getPath() ).getName() );

        file.delete(); // TODO remove recursively
    }

    public Resource createResource( MethodExecutionContext context, String deepPath )
    {
        return new FileResource();
    }

    public long getLastModified()
    {
        return resourcePath.lastModified();
    }

    public long getCreation()
    {
        return resourcePath.lastModified(); // FIXME make java better!
    }

    public static void move( File from, File to )
    {
        if ( from.renameTo( to ) )
        {
            return;
        }

        boolean moved = false;
        InputStream in = null;
        OutputStream out = null;
        try
        {
//            if( File.pathSeparatorChar == ';' )
//                Thread.sleep( 1000L ); // wait for resource to get free under windows
//            
            in = new FileInputStream( from );
            out = new FileOutputStream( to );

            IOUtil.copy( in, out );
            moved = true;
        }
        catch ( Exception e )
        {
            // TODO handle better
            e.printStackTrace();
        }
        finally
        {
            IOUtil.close( in );
            IOUtil.close( out );
        }

        if ( moved )
        {
            from.delete();
        }
    }
}
