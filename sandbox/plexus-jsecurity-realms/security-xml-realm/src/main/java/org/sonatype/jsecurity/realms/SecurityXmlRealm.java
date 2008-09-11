/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.jsecurity.realms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jsecurity.authc.AccountException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.DisabledAccountException;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authc.credential.Sha1CredentialsMatcher;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.subject.PrincipalCollection;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Reader;

/**
 * @plexus.component role="org.jsecurity.realm.Realm" role-hint="SecurityXmlRealm"
 *
 */
public class SecurityXmlRealm
    extends AuthorizingRealm
        implements Initializable
{
    /**
     * @plexus.configuration default-value="${security-xml-file}"
     */
    private File securityConfiguration;
    
    /**
     * This will hold the current configuration in memory, to reload, will need to set this to null
     */
    private Configuration configuration = null;
    
    public void initialize()
        throws InitializationException
    {
        setCredentialsMatcher( new Sha1CredentialsMatcher() );
    }
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        UsernamePasswordToken upToken = ( UsernamePasswordToken ) token;
        
        CUser user = getUser( upToken.getUsername() );
        
        if ( user == null )
        {
            throw new AccountException( "User '" + upToken.getUsername() + "' cannot be retrieved." );
        }
        
        if ( user.getPassword() == null )
        {
            throw new AccountException( "User '" + upToken.getUsername() + "' has no password, cannot authenticate." );
        }
        
        if ( CUser.STATUS_ACTIVE.equals( user.getStatus() ) )
        {
            return new SimpleAuthenticationInfo( upToken.getUsername(), user.getPassword().toCharArray() , getName() );
        }
        else if ( CUser.STATUS_DISABLED.equals( user.getStatus() ) )
        {
            throw new DisabledAccountException( "User '" + upToken.getUsername() + "' is disabled." );
        }
        else
        {
            throw new AccountException( "User '" + upToken.getUsername() + "' is in illegal status '" + user.getStatus() + "'." );
        }
    }
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    private CUser getUser( String userid )
    {
        try
        {
            for ( CUser user : ( List<CUser> ) getConfiguration().getUsers() )
            {
                if ( user.getUserId().equals( userid ) )
                {
                    return user;
                }
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        catch ( XmlPullParserException e )
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private Configuration getConfiguration() 
        throws IOException, 
            XmlPullParserException
    {
        if ( configuration != null )
        {
            return configuration;
        }
        
        Reader fr = null;
        
        FileInputStream is = new FileInputStream( securityConfiguration );

        try
        {
            SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            configuration = reader.read( fr );
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }
        
        return configuration;
    }
}
