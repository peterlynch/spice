/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.codehaus.plexus;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.context.Context;

public final class DefaultPlexusContainer
    implements PlexusContainer
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unused" )
    public DefaultPlexusContainer( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Context getContext()
    {
        return null;
    }

    public Object lookup( final String role )
    {
        return null;
    }

    public Object lookup( final String role, final String roleHint )
    {
        return null;
    }

    public <T> T lookup( final Class<T> type )
    {
        return null;
    }

    public <T> T lookup( final Class<T> type, final String roleHint )
    {
        return null;
    }

    public <T> T lookup( final Class<T> type, final String role, final String roleHint )
    {
        return null;
    }

    public <T> List<T> lookupList( final Class<T> type )
    {
        return null;
    }

    public Map<String, Object> lookupMap( final String role )
    {
        return null;
    }

    public boolean hasComponent( final Class<?> type )
    {
        return false;
    }

    public boolean hasComponent( final Class<?> type, final String roleHint )
    {
        return false;
    }

    public boolean hasComponent( final Class<?> type, final String role, final String roleHint )
    {
        return false;
    }

    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String roleHint )
    {
        return null;
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role,
                                                              final String roleHint )
    {
        return null;
    }

    public List<ComponentDescriptor<?>> getComponentDescriptorList( final String role )
    {
        return null;
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        return null;
    }

    public <T> void addComponentDescriptor( final ComponentDescriptor<T> descriptor )
    {
    }

    public ClassRealm getContainerRealm()
    {
        return null;
    }

    public ClassRealm createChildRealm( final String id )
    {
        return null;
    }

    public List<ComponentDescriptor<?>> discoverComponents( final ClassRealm childRealm )
    {
        return null;
    }

    public void removeComponentRealm( final ClassRealm realm )
    {
    }

    public void release( final Object component )
    {
    }

    public void dispose()
    {
    }
}
