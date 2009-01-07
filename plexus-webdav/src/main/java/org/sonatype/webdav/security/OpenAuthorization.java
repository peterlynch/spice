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
package org.sonatype.webdav.security;

// TODO: Auto-generated Javadoc
/**
 * Class that always authorizes. Used for Nexus, since auth/authz are handled there.
 * 
 * @author cstamas
 * @plexus.component role-hint="open"
 */
public class OpenAuthorization
    implements Authorization
{

    /* (non-Javadoc)
     * @see org.sonatype.webdav.security.Authorization#authorize(org.sonatype.webdav.security.User, org.sonatype.webdav.security.Permission)
     */
    public boolean authorize( User user, Permission permission )
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.sonatype.webdav.security.Authorization#authorize(org.sonatype.webdav.security.User, org.sonatype.webdav.security.Permission, java.lang.String)
     */
    public boolean authorize( User user, Permission permission, String path )
    {
        return true;
    }

}
