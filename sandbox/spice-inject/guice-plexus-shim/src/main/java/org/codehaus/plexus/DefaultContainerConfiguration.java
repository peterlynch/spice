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
import java.util.HashMap;
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
        return this;
    }

    public ContainerConfiguration setContainerConfiguration( final String configuration )
    {
        return this;
    }

    public ContainerConfiguration setContainerConfigurationURL( final URL configuration )
    {
        return this;
    }

    public ContainerConfiguration setClassWorld( final ClassWorld classWorld )
    {
        return this;
    }

    public ContainerConfiguration setRealm( final ClassRealm classRealm )
    {
        return null;
    }

    public ContainerConfiguration setComponentRepository( final ComponentRepository repository )
    {
        return this;
    }

    public ContainerConfiguration setContext( final Map<?, ?> context )
    {
        return this;
    }

    public Map<Object, Object> getContext()
    {
        return new HashMap<Object, Object>();
    }
}
