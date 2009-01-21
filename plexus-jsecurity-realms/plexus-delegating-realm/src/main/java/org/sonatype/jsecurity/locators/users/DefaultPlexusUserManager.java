/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.jsecurity.locators.users;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = PlexusUserManager.class )
public class DefaultPlexusUserManager
    implements PlexusUserManager
{

    @Requirement( role = PlexusUserLocator.class )
    private List<PlexusUserLocator> userlocators;
    

    public Set<PlexusUser> listUsers( String source )
    {
        Set<PlexusUser> users = new TreeSet<PlexusUser>();

        // FIXME add something that would make the primary user win over the other realms?

        for ( PlexusUserLocator locator : userlocators )
        {   
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                users.addAll( locator.listUsers() );
            }
        }

        return users;
    }
    
    public Set<PlexusUser> searchUsers( PlexusUserSearchCriteria criteria, String source )
    {
        Set<PlexusUser> users = new TreeSet<PlexusUser>();
        
        for ( PlexusUserLocator locator : userlocators )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                users.addAll( locator.searchUsers( criteria ) );
            }
        }
        
        return users;
    }
    
    public Set<String> listUserIds( String source )
    {
        Set<String> userIds = new TreeSet<String>();

        for ( PlexusUserLocator locator : userlocators )
        {
            if ( SOURCE_ALL.equals( source ) || locator.getSource().equals( source ) )
            {
                userIds.addAll( locator.listUserIds() );
            }
        }

        return userIds;
    }

    public PlexusUser getUser( String userId )
    {
        PlexusUserLocator primary = getPrimaryLocator();
        PlexusUser user = null;
        if ( primary != null )
        {
            user = primary.getUser( userId );
        }

        if ( user == null )
        {
            for ( PlexusUserLocator locator : userlocators )
            {
                user = locator.getUser( userId );

                if ( user != null )
                {
                    break;
                }
            }
        }

        return user;
    }

    public PlexusUser getUser( String userId, String source )
    {
        PlexusUser user = null;
        if ( SOURCE_ALL.equals( source ) )
        {
            user =  this.getUser( userId );
        }
        else
        {
            for ( PlexusUserLocator locator : userlocators )
            {
                if ( locator.getSource().equals( source ) )
                {
                    user = locator.getUser( userId );
                }
            }
        }
        
        return user;
    }

    private PlexusUserLocator getPrimaryLocator()
    {
        for ( PlexusUserLocator locator : userlocators )
        {
            if ( locator.isPrimary() )
            {
                return locator;
            }
        }

        return null;
    }
}
