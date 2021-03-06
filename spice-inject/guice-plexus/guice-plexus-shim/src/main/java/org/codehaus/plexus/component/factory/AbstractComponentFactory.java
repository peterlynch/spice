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
package org.codehaus.plexus.component.factory;

import org.codehaus.classworlds.ClassRealmAdapter;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentDescriptor;

@SuppressWarnings( "rawtypes" )
public abstract class AbstractComponentFactory
    implements ComponentFactory
{
    public Object newInstance( final ComponentDescriptor cd, final ClassRealm realm, final PlexusContainer container )
        throws ComponentInstantiationException
    {
        return newInstance( cd, ClassRealmAdapter.getInstance( realm ), container );
    }

    @SuppressWarnings( "unused" )
    protected Object newInstance( final ComponentDescriptor cd, final org.codehaus.classworlds.ClassRealm realm,
                                  final PlexusContainer container )
        throws ComponentInstantiationException
    {
        throw new IllegalStateException( getClass() + " does not implement component creation" );
    }
}
