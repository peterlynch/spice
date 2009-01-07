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
package org.sonatype.jsecurity.realms.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUserRoleMapping;
import org.sonatype.jsecurity.realms.validator.ValidationContext;

@Component( role = ConfigurationManager.class, hint = "resourceMerging" )
public class ResourceMergingConfigurationManager
    extends AbstractLogEnabled
    implements ConfigurationManager
{
    //This will handle all normal security.xml file loading/storing
    @Requirement( role = ConfigurationManager.class, hint = "default" )
    private ConfigurationManager manager;
        
    @Requirement( role = StaticSecurityResource.class )
    private List<StaticSecurityResource> staticResources;
    
    /**
     * This will hold the current configuration in memory, to reload, will need to set this to null
     */
    private Configuration configuration = null;
    
    private ReentrantLock lock = new ReentrantLock();
    
    public void clearCache()
    {
        // TODO Auto-generated method stub
        manager.clearCache();
    }
    
    public void createPrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException
    {
        manager.createPrivilege( privilege, initializeContext() );
    }

    public void createPrivilege( SecurityPrivilege privilege, ValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }
        
        // The static config can't be updated, so delegate to xml file
        manager.createPrivilege( privilege, context );
    }
    
    public void createRole( SecurityRole role )
        throws InvalidConfigurationException
    {
        manager.createRole( role, initializeContext() );
    }

    public void createRole( SecurityRole role, ValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }
        
        // The static config can't be updated, so delegate to xml file
        manager.createRole( role, context );
    }

    public void createUser( SecurityUser user )
        throws InvalidConfigurationException
    {
        manager.createUser( user, initializeContext() );
    }
    
    public void createUser( SecurityUser user, String password )
    throws InvalidConfigurationException
    {
        manager.createUser( user, password, initializeContext() );
    }
    
    public void createUser( SecurityUser user, ValidationContext context )
        throws InvalidConfigurationException
    {
        createUser( user, null, context );
    }
    
    public void createUser( SecurityUser user,String password, ValidationContext context )
    throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }
        
        // The static config can't be updated, so delegate to xml file
        manager.createUser( user, password, context );
    }

    public void deletePrivilege( String id )
        throws NoSuchPrivilegeException
    {
        // The static config can't be updated, so delegate to xml file
        manager.deletePrivilege( id );
    }

    public void deleteRole( String id )
        throws NoSuchRoleException
    {
        // The static config can't be updated, so delegate to xml file
        manager.deleteRole( id );
    }

    public void deleteUser( String id )
        throws NoSuchUserException
    {
        // The static config can't be updated, so delegate to xml file
        manager.deleteUser( id );
    }

    public String getPrivilegeProperty( SecurityPrivilege privilege, String key )
    {
        return manager.getPrivilegeProperty( privilege, key );
    }

    public String getPrivilegeProperty( String id, String key )
        throws NoSuchPrivilegeException
    {
        return manager.getPrivilegeProperty( id, key );
    }
    
    public ValidationContext initializeContext()
    {
        ValidationContext context = new ValidationContext();
        
        context.addExistingUserIds();
        context.addExistingRoleIds();
        context.addExistingPrivilegeIds();
        
        for ( CUser user : listUsers() )
        {
            context.getExistingUserIds().add( user.getId() );
            
            context.getExistingEmailMap().put( user.getId(), user.getEmail() );
        }

        for ( CRole role : listRoles() )
        {
            context.getExistingRoleIds().add( role.getId() );

            ArrayList<String> containedRoles = new ArrayList<String>();

            containedRoles.addAll( role.getRoles() );

            context.getRoleContainmentMap().put( role.getId(), containedRoles );
            
            context.getExistingRoleNameMap().put( role.getId(), role.getName() );
        }

        for ( CPrivilege priv : listPrivileges() )
        {
            context.getExistingPrivilegeIds().add( priv.getId() );
        }

        return context;
    }

    public List<SecurityPrivilege> listPrivileges()
    {        
        List<SecurityPrivilege> list = manager.listPrivileges();
        
        for ( CPrivilege item : ( List<CPrivilege> ) getConfiguration().getPrivileges() )
        {
            list.add( new SecurityPrivilege( item, true ) );
        }
        
        return list;
    }

    public List<SecurityRole> listRoles()
    {
        List<SecurityRole> list = manager.listRoles();
        
        for ( CRole item : ( List<CRole> ) getConfiguration().getRoles() )
        {
            list.add( new SecurityRole( item, true ) );
        }
        
        return list;
    }

    public List<SecurityUser> listUsers()
    {
        List<SecurityUser> list = manager.listUsers();
        
        for ( CUser item : ( List<CUser> ) getConfiguration().getUsers() )
        {
            list.add( new SecurityUser( item, true ) );
        }
        
        return list;
    }

    public SecurityPrivilege readPrivilege( String id )
        throws NoSuchPrivilegeException
    {
        for ( CPrivilege privilege : (List<CPrivilege>) getConfiguration().getPrivileges() )
        {
            if ( privilege.getId().equals( id ) )
            {
                return new SecurityPrivilege( privilege, true );
            }
        }

        return manager.readPrivilege( id );
    }

    public SecurityRole readRole( String id )
        throws NoSuchRoleException
    {
        for ( CRole role : (List<CRole>) getConfiguration().getRoles() )
        {
            if ( role.getId().equals( id ) )
            {
                return new SecurityRole( role, true );
            }
        }

        return manager.readRole( id );
    }

    public SecurityUser readUser( String id )
        throws NoSuchUserException
    {
        for ( CUser user : (List<CUser>) getConfiguration().getUsers() )
        {
            if ( user.getId().equals( id ) )
            {
                return new SecurityUser( user, true );
            }
        }

        return manager.readUser( id );
    }
    
    
    
    
    public void createUserRoleMapping( SecurityUserRoleMapping userRoleMapping, ValidationContext context )
        throws InvalidConfigurationException
    {
        if( context == null)
        {
            context = this.initializeContext();
        }
        
        this.manager.createUserRoleMapping( userRoleMapping, context );
    }

    public void createUserRoleMapping( SecurityUserRoleMapping userRoleMapping)
        throws InvalidConfigurationException
    {
        this.manager.createUserRoleMapping( userRoleMapping, this.initializeContext() );
    }

    public void deleteUserRoleMapping( String userId, String source )
        throws NoSuchRoleMappingException
    {
        this.manager.deleteUserRoleMapping( userId, source );        
    }

    public List<SecurityUserRoleMapping> listUserRoleMappings()
    {
        return this.manager.listUserRoleMappings();
    }

    public SecurityUserRoleMapping readUserRoleMapping( String userId, String source )
        throws NoSuchRoleMappingException
    {
        return this.manager.readUserRoleMapping( userId, source );
    }

    public void updateUserRoleMapping( SecurityUserRoleMapping userRoleMapping, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchRoleMappingException
    {
        if( context == null)
        {
            context = this.initializeContext();
        }
        
        this.manager.updateUserRoleMapping( userRoleMapping, context );
    }

    public void updateUserRoleMapping( SecurityUserRoleMapping userRoleMapping )
        throws InvalidConfigurationException,
            NoSuchRoleMappingException
    {
       this.updateUserRoleMapping( userRoleMapping, this.initializeContext() );
    }

    private Configuration initializeStaticConfiguration()
    {
        lock.lock();
        
        try
        {        
            for ( StaticSecurityResource resource : staticResources )
            {
                Configuration existing = resource.getConfiguration();
                
                if ( existing != null )
                {
                    appendConfig( existing );
                }
                else
                {
                    Reader fr = null;
                    InputStream is = null;
        
                    try
                    {
                        getLogger().debug( "Loading static security config from " + resource.getResourcePath() );
                        is = getClass().getResourceAsStream( resource.getResourcePath() );
                        SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();
        
                        fr = new InputStreamReader( is );
                        
                        appendConfig( reader.read( fr ) );
                    }
                    catch ( IOException e )
                    {
                        getLogger().error( "IOException while retrieving configuration file", e );
                    }
                    catch ( XmlPullParserException e )
                    {
                        getLogger().error( "Invalid XML Configuration", e );
                    }
                    finally
                    {
                        if ( fr != null )
                        {
                            try
                            {
                                fr.close();
                            }
                            catch ( IOException e )
                            {
                                // just closing if open
                            }
                        }
                        
                        if ( is != null )
                        {
                            try
                            {
                                is.close();
                            }
                            catch ( IOException e )
                            {
                                // just closing if open
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            if ( configuration == null )
            {
                configuration = new Configuration();
            }
            
            lock.unlock();
        }
        
        return configuration;
    }
    
    private Configuration appendConfig( Configuration config )
    {
        if ( configuration == null )
        {
            configuration = new Configuration();
        }
        
        for ( CPrivilege privilege : ( List<CPrivilege> )config.getPrivileges() )
        {
            configuration.addPrivilege( privilege );
        }
        
        for ( CRole role : ( List<CRole> )config.getRoles() )
        {
            configuration.addRole( role );
        }
        
        for ( CUser user : ( List<CUser> )config.getUsers() )
        {
            configuration.addUser( user );
        }
        
        return configuration;
    }
    
    private Configuration getConfiguration()
    {
        if ( configuration != null )
        {
            return configuration;
        }
        
        return initializeStaticConfiguration();        
    }

    public void save()
    {
        // The static config can't be updated, so delegate to xml file
        manager.save();
    }

    public void updatePrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException,
            NoSuchPrivilegeException
    {
        manager.updatePrivilege( privilege, initializeContext() );
    }
    
    public void updatePrivilege( SecurityPrivilege privilege, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchPrivilegeException
    {
        if ( context == null )
        {
            context = initializeContext();
        }   
        
        // The static config can't be updated, so delegate to xml file
        manager.updatePrivilege( privilege, context );
    }

    public void updateRole( SecurityRole role )
        throws InvalidConfigurationException,
            NoSuchRoleException
    {
        manager.updateRole( role, initializeContext() );
    }
    
    public void updateRole( SecurityRole role, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchRoleException
    {
        if ( context == null )
        {
            context = initializeContext();
        }
        
        // The static config can't be updated, so delegate to xml file
        manager.updateRole( role, context );
    }

    public void updateUser( SecurityUser user )
        throws InvalidConfigurationException,
            NoSuchUserException
    {
        manager.updateUser( user, initializeContext() );
    }
    
    public void updateUser( SecurityUser user, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchUserException
    {
        if ( context == null )
        {
            context = initializeContext();
        }
        
        // The static config can't be updated, so delegate to xml file
        manager.updateUser( user, context );
    }
}
