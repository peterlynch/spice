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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUser;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.jsecurity.realms.validator.ConfigurationValidator;
import org.sonatype.jsecurity.realms.validator.ValidationContext;
import org.sonatype.jsecurity.realms.validator.ValidationResponse;

@Component( role = ConfigurationManager.class, hint = "default" )
public class DefaultConfigurationManager
    extends AbstractLogEnabled
    implements ConfigurationManager
{
    @org.codehaus.plexus.component.annotations.Configuration( value = "${security-xml-file}" )
    private File securityConfiguration;

    @Requirement
    private ConfigurationValidator validator;

    /**
     * This will hold the current configuration in memory, to reload, will need to set this to null
     */
    private Configuration configuration = null;

    private ReentrantLock lock = new ReentrantLock();

    @SuppressWarnings( "unchecked" )
    public List<SecurityPrivilege> listPrivileges()
    {
        List<SecurityPrivilege> list = new ArrayList<SecurityPrivilege>();
        
        for ( CPrivilege item : ( List<CPrivilege> ) getConfiguration().getPrivileges() )
        {
            list.add( new SecurityPrivilege( item ) );
        }
        
        return list;
    }

    @SuppressWarnings( "unchecked" )
    public List<SecurityRole> listRoles()
    {
        List<SecurityRole> list = new ArrayList<SecurityRole>();
        
        for ( CRole item : ( List<CRole> ) getConfiguration().getRoles() )
        {
            list.add( new SecurityRole( item ) );
        }
        
        return list;
    }

    @SuppressWarnings( "unchecked" )
    public List<SecurityUser> listUsers()
    {
        List<SecurityUser> list = new ArrayList<SecurityUser>();
        
        for ( CUser item : ( List<CUser> ) getConfiguration().getUsers() )
        {
            list.add( new SecurityUser( item ) );
        }
        
        return list;
    }
    
    public void createPrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException
    {
        createPrivilege( privilege, initializeContext() );
    }

    public void createPrivilege( SecurityPrivilege privilege, ValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }
        
        ValidationResponse vr = validator.validatePrivilege( context, privilege, false );

        if ( vr.isValid() )
        {
            getConfiguration().addPrivilege( privilege );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void createRole( SecurityRole role )
        throws InvalidConfigurationException
    {
        createRole( role, initializeContext() );
    }
    
    public void createRole( SecurityRole role, ValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }
        
        ValidationResponse vr = validator.validateRole( context, role, false );

        if ( vr.isValid() )
        {
            getConfiguration().addRole( role );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }
    
    public void createUser( SecurityUser user )
        throws InvalidConfigurationException
    {
        createUser( user, null, initializeContext() );
    }
    
    public void createUser( SecurityUser user, String password )
    throws InvalidConfigurationException
    {
        createUser( user, password, initializeContext() );
    }

    public void createUser( SecurityUser user, ValidationContext context )
        throws InvalidConfigurationException
    {
        createUser( user, null, context );
    }
    
    public void createUser( SecurityUser user, String password, ValidationContext context )
    throws InvalidConfigurationException
{
    if ( context == null )
    {
        context = initializeContext();
    }
    
    ValidationResponse vr = validator.validateUser( context, user, false );

    if ( vr.isValid() )
    {
        getConfiguration().addUser( user );
    }
    else
    {
        throw new InvalidConfigurationException( vr );
    }
}

    @SuppressWarnings( "unchecked" )
    public void deletePrivilege( String id )
        throws NoSuchPrivilegeException
    {
        boolean found = false;

        for ( Iterator<CPrivilege> iter = getConfiguration().getPrivileges().iterator(); iter.hasNext(); )
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

    @SuppressWarnings( "unchecked" )
    public void deleteRole( String id )
        throws NoSuchRoleException
    {
        boolean found = false;

        for ( Iterator<CRole> iter = getConfiguration().getRoles().iterator(); iter.hasNext(); )
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

    @SuppressWarnings( "unchecked" )
    public void deleteUser( String id )
        throws NoSuchUserException
    {
        boolean found = false;

        for ( Iterator<CUser> iter = getConfiguration().getUsers().iterator(); iter.hasNext(); )
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

    @SuppressWarnings( "unchecked" )
    public SecurityPrivilege readPrivilege( String id )
        throws NoSuchPrivilegeException
    {
        for ( CPrivilege privilege : (List<CPrivilege>) getConfiguration().getPrivileges() )
        {
            if ( privilege.getId().equals( id ) )
            {
                return new SecurityPrivilege( privilege );
            }
        }

        throw new NoSuchPrivilegeException( id );
    }

    @SuppressWarnings( "unchecked" )
    public SecurityRole readRole( String id )
        throws NoSuchRoleException
    {
        for ( CRole role : (List<CRole>) getConfiguration().getRoles() )
        {
            if ( role.getId().equals( id ) )
            {
                return new SecurityRole( role );
            }
        }

        throw new NoSuchRoleException( id );
    }

    @SuppressWarnings( "unchecked" )
    public SecurityUser readUser( String id )
        throws NoSuchUserException
    {
        for ( CUser user : (List<CUser>) getConfiguration().getUsers() )
        {
            if ( user.getId().equals( id ) )
            {
                return new SecurityUser( user );
            }
        }

        throw new NoSuchUserException( id );
    }
    
    public void updatePrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException,
            NoSuchPrivilegeException
    {
        updatePrivilege( privilege, initializeContext() );
    }

    public void updatePrivilege( SecurityPrivilege privilege, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchPrivilegeException
    {
        if ( context == null )
        {
            context = initializeContext();
        }
        
        ValidationResponse vr = validator.validatePrivilege( context, privilege, true );

        if ( vr.isValid() )
        {
            deletePrivilege( privilege.getId() );
            getConfiguration().addPrivilege( privilege );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void updateRole( SecurityRole role )
        throws InvalidConfigurationException,
            NoSuchRoleException
    {
        updateRole( role, initializeContext() );
    }
    
    public void updateRole( SecurityRole role, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchRoleException
    {
        if ( context == null )
        {
            context = initializeContext();
        }
        
        ValidationResponse vr = validator.validateRole( context, role, true );

        if ( vr.isValid() )
        {
            deleteRole( role.getId() );
            getConfiguration().addRole( role );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void updateUser( SecurityUser user )
        throws InvalidConfigurationException,
            NoSuchUserException
    {
        updateUser( user, initializeContext() );
    }
    
    public void updateUser( SecurityUser user, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchUserException
    {
        if ( context == null )
        {
            context = initializeContext();
        }
        
        ValidationResponse vr = validator.validateUser( context, user, true );

        if ( vr.isValid() )
        {
            deleteUser( user.getId() );
            getConfiguration().addUser( user );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    @SuppressWarnings( "unchecked" )
    public String getPrivilegeProperty( SecurityPrivilege privilege, String key )
    {
        if ( privilege != null && privilege.getProperties() != null )
        {
            for ( CProperty prop : (List<CProperty>) privilege.getProperties() )
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
        FileInputStream is = null;

        try
        {
            is = new FileInputStream( securityConfiguration );

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

            lock.unlock();
        }

        return configuration;
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
        }

        for ( CPrivilege priv : listPrivileges() )
        {
            context.getExistingPrivilegeIds().add( priv.getId() );
        }

        return context;
    }
}
