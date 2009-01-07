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
package org.sonatype.plexus.jetty;

import java.lang.reflect.Constructor;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.StringUtils;

/**
 * A simple base class that encapsulates how we get some Jetty component/part. It could be simple Java instantaniation
 * with parameters (a la Jason) or lookin up Plexus components.
 * 
 * @author cstamas
 */
public abstract class JettyComponent
{
    private String role;

    private String roleHint;

    private String clazz;

    public String getRole()
    {
        return role;
    }

    public void setRole( String role )
    {
        this.role = role;
    }

    public String getRoleHint()
    {
        return roleHint;
    }

    public void setRoleHint( String roleHint )
    {
        this.roleHint = roleHint;
    }

    public String getClazz()
    {
        return clazz;
    }

    public void setClazz( String clazz )
    {
        this.clazz = clazz;
    }

    protected Object instantiate( Context context )
        throws Exception
    {
        if ( getClazz() != null )
        {
            Class clazz;

            int index = getClazz().indexOf( "(" );

            // This could obviously be more general but we just want this to work with the Restlet handler
            // right now. We should take something like this:
            //
            // org.sonatype.plexus.jetty.MockRestletHandler(#server)
            //
            // and parse out the parameters and take the #expressions out of a Map like OGNL, but
            // this will do for the short term. I will fix this on the weekend.

            if ( index > 0 )
            {
                String parametersString = getClazz().substring( index + 1, getClazz().length() - 1 );

                String[] parameters = StringUtils.split( parametersString, "," );

                Class[] clazzesParameters = new Class[parameters.length];

                Object[] objectParameters = new Object[parameters.length];

                for ( int i = 0; i < parameters.length; i++ )
                {
                    // Get rid of the #
                    String parameter = parameters[i].substring( 1 );

                    Object o = context.get( parameter );

                    clazzesParameters[i] = o.getClass();

                    objectParameters[i] = o;
                }

                String className = getClazz().substring( 0, index );

                clazz = getClass().getClassLoader().loadClass( className );

                Constructor constructor = clazz.getConstructor( clazzesParameters );

                return constructor.newInstance( objectParameters );
            }
            else
            {
                clazz = getClass().getClassLoader().loadClass( getClazz() );

                return clazz.newInstance();
            }

        }
        else
        {
            if ( getRoleHint() != null )
            {
                return ( (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY ) )
                    .lookup( getRole(), getRoleHint() );
            }
            else
            {
                return ( (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY ) ).lookup( getRole() );
            }
        }
    }
}
