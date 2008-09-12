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
import java.util.List;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.jsecurity.model.CPrivilege;
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
    
    public void createPrivilege( CPrivilege privilege )
    {
        getConfiguration().addPrivilege( clonePrivilege( privilege ) );
    }

    public void createRole( CRole role )
    {
        getConfiguration().addRole( cloneRole( role ) );
    }

    public void createUser( CUser user )
    {
        getConfiguration().addUser( cloneUser( user ) );
    }

    public void deletePrivilege( String id )
    {
        getConfiguration().removePrivilege( readPrivilege( id ) );
    }

    public void deleteRole( String id )
    {
        getConfiguration().removeRole( readRole( id ) );
    }

    public void deleteUser( String id )
    {
        getConfiguration().removeUser( readUser( id ) );
    }

    public CPrivilege readPrivilege( String id )
    {
        for ( CPrivilege privilege : ( List<CPrivilege> ) getConfiguration().getPrivileges() )
        {
            if ( privilege.getId().equals( id ) )
            {
                return clonePrivilege( privilege );
            }
        }
        
        return null;
    }

    public CRole readRole( String id )
    {
        for ( CRole role : ( List<CRole> ) getConfiguration().getRoles() )
        {
            if ( role.getId().equals( id ) )
            {
                return cloneRole( role );
            }
        }
        
        return null;
    }

    public CUser readUser( String id )
    {
        for ( CUser user : ( List<CUser> ) getConfiguration().getUsers() )
        {
            if ( user.getId().equals( id ) )
            {
                return cloneUser( user );
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
    
    public void clearCache()
    {
        configuration = null;
    }
    
    public void save()
    {
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
        }
    }
    
    private CUser cloneUser( CUser user )
    {
        if ( user == null )
        {
            return null;
        }
        
        CUser cloned = new CUser();
        
        cloned.setEmail( user.getEmail() );
        cloned.setName( user.getName() );
        cloned.setPassword( user.getPassword() );
        cloned.setStatus( user.getStatus() );
        cloned.setId( user.getId() );
     
        if ( user.getRoles() != null )
        {
            for ( String roleId : ( List<String> ) user.getRoles() )
            {
                cloned.addRole( roleId );
            }
        }
        
        return cloned;
    }
    
    private CRole cloneRole( CRole role )
    {
        if ( role == null )
        {
            return null;
        }
        
        CRole cloned = new CRole();
        
        cloned.setDescription( role.getDescription() );
        cloned.setId( role.getId() );
        cloned.setName( role.getName() );
        cloned.setSessionTimeout( role.getSessionTimeout() );
        
        if ( role.getRoles() != null )
        {
            for ( String roleId : ( List<String> ) role.getRoles() )
            {
                cloned.addRole( roleId );
            }
        }
        
        if ( role.getPrivileges() != null )
        {
            for ( String privilegeId : ( List<String> ) role.getPrivileges() )
            {
                cloned.addPrivilege( privilegeId );
            }
        }
        
        return cloned;
    }
    
    private CPrivilege clonePrivilege( CPrivilege privilege )
    {
        if ( privilege == null )
        {
            return privilege;
        }
        
        CPrivilege cloned = new CPrivilege();
        
        cloned.setDescription( privilege.getDescription() );
        cloned.setId( privilege.getId() );
        cloned.setMethod( privilege.getMethod() );
        cloned.setName( privilege.getName() );
        cloned.setPermission( privilege.getPermission() );
        
        return cloned;
    }
    
    private Configuration getConfiguration()
    {
        if ( configuration != null )
        {
            return configuration;
        }
        
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
        }
        
        return configuration;
    }

}
