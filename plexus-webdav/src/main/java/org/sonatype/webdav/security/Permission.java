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
package org.sonatype.webdav.security;

/**
 * A simple security role class.
 * User defined roles are allowed, but the default ones are listed as static members.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class Permission
{
    public static final Permission PERMISSION_REPOSITORY_READ = new Permission( "repository.read" );

    public static final Permission PERMISSION_REPOSITORY_WRITE = new Permission( "repository.write" );

    public static final Permission PERMISSION_SITE_REPOSITORY_READ = new Permission( "repository.read.read" );

    public static final Permission PERMISSION_SITE_REPOSITORY_WRITE = new Permission( "repository.site.write" );

    private String role;

    public Permission( String id )
    {
        this.role = id;
    }

    public String getId()
    {
        return role;
    }

    public String toString()
    {
        return "WebdavRole: " + role;
    }

    public boolean equals( Object compare )
    {
        if ( !( compare instanceof Permission ) )
        {
            return false;
        }

        return role.equals( ( (Permission) compare ).getId() );
    }
}