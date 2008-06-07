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
package org.sonatype.webdav;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Andrew Williams
 * @plexus.component role="org.sonatype.webdav.Resource" role-hint="file"
 */
public class FileResource
    extends AbstractResource
{
    private File backing;

    public FileResource()
    {
        super();

        try
        {
            backing = File.createTempFile( "webdav", ".tmp" );
        }
        catch ( IOException e )
        {
            // not sure what to do here
        }
    }

    public FileResource( File file )
    {
        backing = file;
    }

    public InputStream streamContent( MethodExecutionContext context )
        throws IOException
    {
        return new FileInputStream( backing );
    }

    public void setContent( InputStream is )
        throws IOException
    {
        OutputStream output = new FileOutputStream( backing );

        copy( is, output, 1024 );
    }

    public boolean getExists()
    {
        return backing.exists();
    }

    public Resource copy( MethodExecutionContext context )
    {
        // FIXME - make this not possibly conflict
        File copy = new File( backing.getParentFile(), backing.getName() + "-tmp" );

        try
        {
            copy( new FileInputStream( backing ), new FileOutputStream( copy ), 1024 );
        }
        catch ( Exception e )
        {
            // not sure what to do here...
        }

        return new FileResource( copy );
    }

    public void remove( MethodExecutionContext context )
    {
        getFile().delete();
    }

    public String getName()
    {
        return backing.getName();
    }

    public void setName( String name )
    {
        File newName = new File( backing.getParent(), name );

        // This should never fail, the conflict could be existing name, but we should have caught that
        backing.renameTo( newName );
        setFile( newName );
    }

    public long getContentLength()
    {
        return backing.length();
    }

    public long getLastModified()
    {
        return backing.lastModified();
    }

    public long getCreation()
    {
        return backing.lastModified(); // FIXME make java better!
    }

    public File getFile()
    {
        return backing;
    }

    void setFile( File backing )
    {
        this.backing = backing;
    }
}
