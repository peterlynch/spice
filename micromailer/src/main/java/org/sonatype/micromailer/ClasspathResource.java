/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.micromailer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * A Resource that takes it's content from classpath.
 * 
 * @author cstamas
 */
public class ClasspathResource
    implements DataSource
{
    private final String path;

    private final String name;

    private final String contentType;

    public ClasspathResource( String path, String name, String contentType )
    {
        super();

        this.path = path;

        this.name = name;

        this.contentType = contentType;
    }

    public String getPath()
    {
        return path;
    }

    public String getName()
    {
        return name;
    }

    public String getContentType()
    {
        return contentType;
    }

    public InputStream getInputStream()
        throws IOException
    {
        return this.getClass().getResourceAsStream( getPath() );
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        throw new UnsupportedOperationException( "Classpath resource is not writable!" );
    }

}
