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

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class SimpleUser
    implements User
{
    public static final User ANONYMOUS_USER = new SimpleUser(ANONYMOUS);
    
    private String username;

    private String email;

    public SimpleUser( String username )
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public boolean isAnonymous()
    {
        return ANONYMOUS.equals( getUsername() );
    }
}
