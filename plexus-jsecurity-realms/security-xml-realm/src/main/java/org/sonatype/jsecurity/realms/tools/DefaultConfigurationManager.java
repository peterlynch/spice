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
     * This will hold the current configuration in memory, to reload, will need to set this to null
     */
    private Configuration configuration = null;
    
    private ReentrantLock lock = new ReentrantLock();
    
    public void createPrivilege( CPrivilege privilege )
    {
        getConfiguration().addPrivilege( ObjectCloner.clone( privilege ) );
    }

    public void createRole( CRole role )
    {
        getConfiguration().addRole( ObjectCloner.clone( role ) );
    }

    public void createUser( CUser user )
    {
        getConfiguration().addUser( ObjectCloner.clone( user ) );
    }

    @SuppressWarnings("unchecked")
    public void deletePrivilege( String id )
    {
        for ( Iterator<CPrivilege> iter = getConfiguration().getPrivileges().iterator() ; iter.hasNext() ; )
        {
            if ( iter.next().getId().equals( id ) )
            {
                iter.remove();
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void deleteRole( String id )
    {
        for ( Iterator<CRole> iter = getConfiguration().getRoles().iterator() ; iter.hasNext() ; )
        {
            if ( iter.next().getId().equals( id ) )
            {
                iter.remove();
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void deleteUser( String id )
    {
        for ( Iterator<CUser> iter = getConfiguration().getUsers().iterator() ; iter.hasNext() ; )
        {
            if ( iter.next().getId().equals( id ) )
            {
                iter.remove();
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public CPrivilege readPrivilege( String id )
    {
        for ( CPrivilege privilege : ( List<CPrivilege> ) getConfiguration().getPrivileges() )
        {
            if ( privilege.getId().equals( id ) )
            {
                return ObjectCloner.clone( privilege );
            }
        }
        
        return null;
    }

    @SuppressWarnings("unchecked")
    public CRole readRole( String id )
    {
        for ( CRole role : ( List<CRole> ) getConfiguration().getRoles() )
        {
            if ( role.getId().equals( id ) )
            {
                return ObjectCloner.clone( role );
            }
        }
        
        return null;
    }

    @SuppressWarnings("unchecked")
    public CUser readUser( String id )
    {
        for ( CUser user : ( List<CUser> ) getConfiguration().getUsers() )
        {
            if ( user.getId().equals( id ) )
            {
                return ObjectCloner.clone( user );
            }
        }
        
        return null;
    }

    public void updatePrivilege( CPrivilege privilege )
    {
        deletePrivilege( privilege.getId() );
        createPrivilege( privilege );
    }

    public void updateRole( CRole role )
    {
        deleteRole( role.getId() );
        createRole( role );
    }

    public void updateUser( CUser user )
    {
        deleteUser( user.getId() );
        createUser( user );
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

}
