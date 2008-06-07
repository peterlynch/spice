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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andrew Williams
 * @plexus.component role="org.sonatype.webdav.Resource" role-hint="memory"
 */
public class MemoryResource
    extends AbstractResource
{
    private String name;

    private byte[] content;

    private boolean exists = true;

    private long created = System.currentTimeMillis();

    private long modified = System.currentTimeMillis();

    public MemoryResource()
    {
        super();
    }

    public MemoryResource( String name )
    {
        setName( name );
    }

    public InputStream streamContent( MethodExecutionContext context )
        throws IOException
    {
        return new ByteArrayInputStream( content );
    }

    public void setContent( InputStream is )
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        copy( is, output, 1024 );

        content = output.toByteArray();

        modified = System.currentTimeMillis();
    }

    public boolean getExists()
    {
        return exists;
    }

    public Resource copy( MethodExecutionContext context )
    {
        MemoryResource ret = new MemoryResource( getName() );
        ret.content = content;
        ret.exists = exists;

        return ret;
    }

    public void remove( MethodExecutionContext context )
    {
        content = null;
        exists = false;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public long getContentLength()
    {
        if ( content == null )
        {
            return 0;
        }

        return content.length;
    }

    public long getLastModified()
    {
        return modified;
    }

    public long getCreation()
    {
        return created;
    }
}
