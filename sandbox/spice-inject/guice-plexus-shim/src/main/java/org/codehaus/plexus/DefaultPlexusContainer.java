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

public final class DefaultPlexusContainer
    implements PlexusContainer
{
    @SuppressWarnings( "unused" )
    public DefaultPlexusContainer( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
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

    public void release( final Object component )
    {
    }

    public void dispose()
    {
    }
}
