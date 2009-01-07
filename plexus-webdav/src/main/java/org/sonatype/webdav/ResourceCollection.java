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
