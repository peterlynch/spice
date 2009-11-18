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

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.context.Context;

public interface PlexusContainer
{
    Context getContext();

    Object lookup( String role )
        throws ComponentLookupException;

    Object lookup( String role, String roleHint )
        throws ComponentLookupException;

    <T> T lookup( Class<T> type )
        throws ComponentLookupException;

    <T> T lookup( Class<T> type, String roleHint )
        throws ComponentLookupException;

    <T> T lookup( Class<T> type, String role, String roleHint )
        throws ComponentLookupException;

    <T> List<T> lookupList( Class<T> type )
        throws ComponentLookupException;

    Map<String, Object> lookupMap( String role )
        throws ComponentLookupException;

    boolean hasComponent( Class<?> type );

    boolean hasComponent( Class<?> type, String roleHint );

    boolean hasComponent( Class<?> type, String role, String roleHint );

    ComponentDescriptor<?> getComponentDescriptor( String role, String roleHint );

    <T> ComponentDescriptor<T> getComponentDescriptor( Class<T> type, String role, String roleHint );

    List<ComponentDescriptor<?>> getComponentDescriptorList( String role );

    <T> List<ComponentDescriptor<T>> getComponentDescriptorList( Class<T> type, String role );

    <T> void addComponentDescriptor( ComponentDescriptor<T> descriptor )
        throws CycleDetectedInComponentGraphException;

    ClassRealm getContainerRealm();

    ClassRealm createChildRealm( String id );

    List<ComponentDescriptor<?>> discoverComponents( ClassRealm childRealm )
        throws PlexusConfigurationException, CycleDetectedInComponentGraphException;

    void removeComponentRealm( ClassRealm realm )
        throws PlexusContainerException;

    void release( Object component )
        throws ComponentLifecycleException;

    void dispose();
}
