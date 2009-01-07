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
package org.sonatype.micromailer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * A Resource backed by file.
 * 
 * @author cstamas
 */
public class FileResource
    implements DataSource
{
    private final File file;

    private final String contentType;

    public FileResource( File file, String contentType )
    {
        super();

        this.file = file;

        this.contentType = contentType;
    }

    public String getName()
    {
        return file.getName();
    }

    public String getContentType()
    {
        return contentType;
    }

    public InputStream getInputStream()
        throws FileNotFoundException
    {
        return new FileInputStream( file );
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        return new FileOutputStream( file );
    }
}
