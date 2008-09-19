package org.sonatype.jsecurity.realms.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.jsecurity.realms.validator.ConfigurationValidator;
import org.sonatype.jsecurity.realms.validator.ValidationContext;
import org.sonatype.jsecurity.realms.validator.ValidationResponse;

/**
 * @plexus.component
 */
public class DefaultConfigurationManager
    extends AbstractLogEnabled
        implements ConfigurationManager
{
    /**
     * @plexus.configuration default-value="${security-xml-file}"
     */
    private File securityConfiguration;
    
    /**
     * @plexus.requirement
     */
    private ConfigurationValidator validator;
    
    /**
     * This will hold the current configuration in memory, to reload, will need to set this to null
     */
    private Configuration configuration = null;
    
    private ReentrantLock lock = new ReentrantLock();
    
    @SuppressWarnings("unchecked")
    public List<CPrivilege> listPrivileges()
    {
        return ( List<CPrivilege> ) getConfiguration().getPrivileges();
    }
    
    @SuppressWarnings("unchecked")
    public List<CRole> listRoles()
    {
        return ( List<CRole> ) getConfiguration().getRoles();
    }
    
    @SuppressWarnings("unchecked")
    public List<CUser> listUsers()
    {
        return ( List<CUser> ) getConfiguration().getUsers();
    }
    
    public void createPrivilege( CPrivilege privilege )
        throws InvalidConfigurationException
    {
        ValidationResponse vr = validator.validatePrivilege( initializeContext(), privilege, false );
        
        if ( vr.isValid() )
        {
            getConfiguration().addPrivilege( ObjectCloner.clone( privilege ) );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void createRole( CRole role )
        throws InvalidConfigurationException
    {
        ValidationResponse vr = validator.validateRole( initializeContext(), role, false );
        
        if ( vr.isValid() )
        {
            getConfiguration().addRole( ObjectCloner.clone( role ) );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void createUser( CUser user )
        throws InvalidConfigurationException
    {        
        ValidationResponse vr = validator.validateUser( initializeContext(), user, false );
        
        if ( vr.isValid() )
        {
            getConfiguration().addUser( ObjectCloner.clone( user ) );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    @SuppressWarnings("unchecked")
    public void deletePrivilege( String id )
        throws NoSuchPrivilegeException
    {
        boolean found = false;
        
        for ( Iterator<CPrivilege> iter = getConfiguration().getPrivileges().iterator() ; iter.hasNext() ; )
        {
            if ( iter.next().getId().equals( id ) )
            {
                found = true;
                iter.remove();
                break;
            }
        }
        
        if ( !found )
        {
            throw new NoSuchPrivilegeException( id );
        }
    }

    @SuppressWarnings("unchecked")
    public void deleteRole( String id )
        throws NoSuchRoleException
    {
        boolean found = false;
        
        for ( Iterator<CRole> iter = getConfiguration().getRoles().iterator() ; iter.hasNext() ; )
        {
            if ( iter.next().getId().equals( id ) )
            {
                found = true;
                iter.remove();
                break;
            }
        }
        
        if ( !found )
        {
            throw new NoSuchRoleException( id );
        }
    }

    @SuppressWarnings("unchecked")
    public void deleteUser( String id )
        throws NoSuchUserException
    {
        boolean found = false;
        
        for ( Iterator<CUser> iter = getConfiguration().getUsers().iterator() ; iter.hasNext() ; )
        {
            if ( iter.next().getId().equals( id ) )
            {
                found = true;
                iter.remove();
                break;
            }
        }
        
        if ( !found )
        {
            throw new NoSuchUserException( id );
        }        
    }

    @SuppressWarnings("unchecked")
    public CPrivilege readPrivilege( String id )
        throws NoSuchPrivilegeException
    {
        for ( CPrivilege privilege : ( List<CPrivilege> ) getConfiguration().getPrivileges() )
        {
            if ( privilege.getId().equals( id ) )
            {
                return ObjectCloner.clone( privilege );
            }
        }
        
        throw new NoSuchPrivilegeException( id );
    }

    @SuppressWarnings("unchecked")
    public CRole readRole( String id )
        throws NoSuchRoleException
    {
        for ( CRole role : ( List<CRole> ) getConfiguration().getRoles() )
        {
            if ( role.getId().equals( id ) )
            {
                return ObjectCloner.clone( role );
            }
        }
        
        throw new NoSuchRoleException( id );
    }

    @SuppressWarnings("unchecked")
    public CUser readUser( String id )
        throws NoSuchUserException
    {
        for ( CUser user : ( List<CUser> ) getConfiguration().getUsers() )
        {
            if ( user.getId().equals( id ) )
            {
                return ObjectCloner.clone( user );
            }
        }
        
        throw new NoSuchUserException( id );
    }

    public void updatePrivilege( CPrivilege privilege )
        throws InvalidConfigurationException,
        NoSuchPrivilegeException
    {
        ValidationResponse vr = validator.validatePrivilege( initializeContext(), privilege, true );

        if ( vr.isValid() )
        {
            deletePrivilege( privilege.getId() );
            getConfiguration().addPrivilege( ObjectCloner.clone( privilege ) );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void updateRole( CRole role )
        throws InvalidConfigurationException,
        NoSuchRoleException
    {
        ValidationResponse vr = validator.validateRole( initializeContext(), role, true );

        if ( vr.isValid() )
        {
            deleteRole( role.getId() );
            getConfiguration().addRole( ObjectCloner.clone( role ) );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void updateUser( CUser user )
        throws InvalidConfigurationException,
        NoSuchUserException
    {
        ValidationResponse vr = validator.validateUser( initializeContext(), user, true );

        if ( vr.isValid() )
        {
            deleteUser( user.getId() );
            getConfiguration().addUser( ObjectCloner.clone( user ) );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getPrivilegeProperty( CPrivilege privilege, String key )
    {
        if ( privilege != null && privilege.getProperties() != null )
        {
            for ( CProperty prop : ( List<CProperty> ) privilege.getProperties() )
            {
                if ( prop.getKey().equals( key ) )
                {
                    return prop.getValue();
                }
            }
        }
        
        return null;
    }
    
    public String getPrivilegeProperty( String id, String key )
        throws NoSuchPrivilegeException
    {
        return getPrivilegeProperty( readPrivilege( id ), key );
    }
    
    public void clearCache()
    {
        // Just to make sure we aren't fiddling w/ save/loading process
        lock.lock();
        configuration = null;
        lock.unlock();
    }
    
    public void save()
    {
        lock.lock();
        
        securityConfiguration.getParentFile().mkdirs();
        
        Writer fw = null;
        
        try
        {
            fw = new OutputStreamWriter( new FileOutputStream( securityConfiguration ) );

            SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();

            writer.write( fw, configuration );
        }
        catch ( IOException e )
        {
            getLogger().error( "IOException while storing configuration file", e );
        }
        finally
        {
            if ( fw != null )
            {
                try
                {
                    fw.flush();
                    
                    fw.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }
            
            lock.unlock();
        }
    }
    
    private Configuration getConfiguration()
    {
        if ( configuration != null )
        {
            return configuration;
        }
        
        lock.lock();
        
        Reader fr = null;
    
        try
        {
            FileInputStream is = new FileInputStream( securityConfiguration );
            
            SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();
    
            fr = new InputStreamReader( is );
    
            configuration = reader.read( fr );
        }
        catch ( FileNotFoundException e )
        {
            // This is ok, may not exist first time around
            configuration = new Configuration();
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
            
            lock.unlock();
        }
        
        return configuration;
    }
    
    private ValidationContext initializeContext()
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
        }

        for ( CPrivilege priv : listPrivileges() )
        {
            context.getExistingPrivilegeIds().add( priv.getId() );
        }
        
        return context;
    }
}
