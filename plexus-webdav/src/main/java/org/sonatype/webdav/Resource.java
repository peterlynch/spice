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

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andrew Williams
 */
public interface Resource
{
    public InputStream streamContent( MethodExecutionContext context )
        throws IOException;

    public void setContent( InputStream is )
        throws IOException;

    public Resource copy( MethodExecutionContext context )
        throws ResourceException,
            UnauthorizedException;

    public void remove( MethodExecutionContext context )
        throws ResourceException,
            UnauthorizedException;

    public String getName();

    public void setName( String name );

    public boolean getExists();

    public String getETag( boolean b );

    public String getETag();

    public long getContentLength();

    public long getLastModified();

    public String getLastModifiedHttp();

    public String getMimeType();

    public void setMimeType( String type );

    public long getCreation();
}
