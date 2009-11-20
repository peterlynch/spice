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
        throw new UnsupportedOperationException( "SHIM" );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Context getContext()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public Object lookup( final String role )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public Object lookup( final String role, final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> T lookup( final Class<T> type )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> T lookup( final Class<T> type, final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> T lookup( final Class<T> type, final String role, final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> List<T> lookupList( final Class<T> type )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public Map<String, Object> lookupMap( final String role )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public boolean hasComponent( final Class<?> type )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public boolean hasComponent( final Class<?> type, final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public boolean hasComponent( final Class<?> type, final String role, final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role, final String hint )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public List<ComponentDescriptor<?>> getComponentDescriptorList( final String role )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public <T> void addComponentDescriptor( final ComponentDescriptor<T> descriptor )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ClassRealm getContainerRealm()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ClassRealm createChildRealm( final String id )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public List<ComponentDescriptor<?>> discoverComponents( final ClassRealm childRealm )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void removeComponentRealm( final ClassRealm realm )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void release( final Object component )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public void dispose()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }
}
