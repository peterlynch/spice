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
