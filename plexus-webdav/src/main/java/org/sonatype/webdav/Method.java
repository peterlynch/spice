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

import org.sonatype.webdav.security.Authorization;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The interface for all webdav methods
 * 
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface Method
{
    public void setAuthorization( Authorization authz );

    public void setResourceCollection( ResourceCollection collection );

    void setSecret( String secret );

    boolean isReadOnly();

    void setReadOnly( boolean readOnly );

    int getDebug();

    void setDebug( int debug );

    boolean getListings();

    void setListings( boolean listings );

    void setFileEncoding( String fileEncoding );

    void execute( MethodExecutionContext context, HttpServletRequest req, HttpServletResponse res )
        throws IOException, UnauthorizedException;
}
