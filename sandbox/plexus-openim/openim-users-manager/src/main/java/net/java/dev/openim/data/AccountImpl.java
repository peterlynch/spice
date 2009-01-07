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
package net.java.dev.openim.data;

import java.io.Serializable;

import net.java.dev.openim.tools.Digester;

/**
 * @version 1.5
 * @author AlAg
 */
public class AccountImpl
    implements Serializable, Account
{

    private static final long serialVersionUID = 15L;

    private String name;
    private String password;

    
    
    public final void setName( String name )
    {
        this.name = name;
    }

    public final String getName()
    {
        return name;
    }

    public final void setPassword( String password )
    {
        this.password = password;
    }

    public final String getPassword()
    {
        return password;
    }

    // -----------------------------------------------------------------------
    public boolean isAuthenticationTypeSupported( int type )
    {
        boolean isSupported = false;
        if ( type == Account.AUTH_TYPE_DIGEST || type == Account.AUTH_TYPE_PLAIN )
        {
            isSupported = true;
        }
        return isSupported;
    }

    // -----------------------------------------------------------------------
    public final void authenticate( int type, String value, String sessionId )
        throws Exception
    {
        if ( type == AUTH_TYPE_PLAIN )
        {
            if ( !password.equals( value ) )
            {
                throw new Exception( "Unvalid plain password" );
            }

        }
        else if ( type == AUTH_TYPE_DIGEST )
        {
            String digest = Digester.digest( sessionId + password );

            if ( !digest.equals( value ) )
            {
                throw new Exception( "Unvalid digest password" + " \nGot   : " + value + "\nExpect: " + digest );
            }

        }
    }

    // -----------------------------------------------------------------------
    public String toString()
    {
        String s = "Username: " + name + " password: " + password;
        return s;
    }

}
