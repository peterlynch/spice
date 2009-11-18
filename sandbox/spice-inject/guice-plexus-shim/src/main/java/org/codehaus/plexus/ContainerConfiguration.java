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
package org.codehaus.plexus;

import java.net.URL;
import java.util.Map;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentRepository;

public interface ContainerConfiguration
{
    ContainerConfiguration setName( String name );

    ContainerConfiguration setContainerConfiguration( String configuration );

    ContainerConfiguration setContainerConfigurationURL( URL configuration );

    ContainerConfiguration setClassWorld( ClassWorld classWorld );

    ContainerConfiguration setRealm( ClassRealm classRealm );

    ContainerConfiguration setComponentRepository( ComponentRepository repository );

    ContainerConfiguration setContext( Map<?, ?> context );

    Map<Object, Object> getContext();
}
