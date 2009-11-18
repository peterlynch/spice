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

import java.util.List;

@SuppressWarnings( "unused" )
public class ComponentSetDescriptor
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void setId( final String id )
    {
    }

    public String getSource()
    {
        return null;
    }

    public void setSource( final String source )
    {
    }

    public List<ComponentDescriptor<?>> getComponents()
    {
        return null;
    }

    public void addComponentDescriptor( final ComponentDescriptor<?> descriptor )
    {
    }

    public void addDependency( final ComponentDependency dependency )
    {
    }
}
