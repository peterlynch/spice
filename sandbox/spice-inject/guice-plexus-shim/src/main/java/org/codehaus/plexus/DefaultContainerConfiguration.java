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

import java.net.URL;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentRepository;

public final class DefaultContainerConfiguration
    implements ContainerConfiguration
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public ContainerConfiguration setName( final String name )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ContainerConfiguration setContainerConfiguration( final String configuration )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ContainerConfiguration setContainerConfigurationURL( final URL configuration )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ContainerConfiguration setClassWorld( final ClassWorld classWorld )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ContainerConfiguration setRealm( final ClassRealm classRealm )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ContainerConfiguration setComponentRepository( final ComponentRepository repository )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public ContainerConfiguration setContext( final Map<?, ?> context )
    {
        throw new UnsupportedOperationException( "SHIM" );
    }

    public Map<Object, Object> getContext()
    {
        throw new UnsupportedOperationException( "SHIM" );
    }
}
