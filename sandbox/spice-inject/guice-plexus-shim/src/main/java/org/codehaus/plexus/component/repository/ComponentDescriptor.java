/**
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.codehaus.plexus.component.repository;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.configuration.PlexusConfiguration;

@SuppressWarnings( "unused" )
public final class ComponentDescriptor<T>
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getDescription()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void setRole( final String role )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void setRoleHint( final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void setInstantiationStrategy( final String instantiationStrategy )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public String getRole()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public String getRoleHint()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public String getInstantiationStrategy()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public Class<T> getRoleClass()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void setImplementation( final String implementation )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public String getImplementation()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public Class<? extends T> getImplementationClass()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void addRequirement( final ComponentRequirement requirement )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void setConfiguration( final PlexusConfiguration configuration )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void setComponentSetDescriptor( final ComponentSetDescriptor setDescriptor )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void setRealm( final ClassRealm realm )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ClassRealm getRealm()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }
}
