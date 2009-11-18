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
        return null;
    }

    public String getRole()
    {
        return null;
    }

    public String getRoleHint()
    {
        return null;
    }

    public String getInstantiationStrategy()
    {
        return null;
    }

    public Class<T> getRoleClass()
    {
        return null;
    }

    public void setRole( final String role )
    {
    }

    public void setRoleHint( final String roleHint )
    {
    }

    public void setInstantiationStrategy( final String instantiationStrategy )
    {
    }

    public String getImplementation()
    {
        return null;
    }

    public Class<? extends T> getImplementationClass()
    {
        return null;
    }

    public void setImplementation( final String implementation )
    {
    }

    public void addRequirement( final ComponentRequirement requirement )
    {
    }

    public void setConfiguration( final PlexusConfiguration configuration )
    {

    }

    public void setComponentSetDescriptor( final ComponentSetDescriptor setDescriptor )
    {
    }

    public ClassRealm getRealm()
    {
        return null;
    }

    public void setRealm( final ClassRealm realm )
    {
    }
}
