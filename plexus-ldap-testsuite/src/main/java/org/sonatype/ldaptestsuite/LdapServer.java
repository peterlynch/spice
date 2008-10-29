/*
 * Copyright 2005.-2007. Tamas Cservenak (t.cservenak@gmail.com)
 * 
 */
package org.sonatype.ldaptestsuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.directory.server.configuration.MutableServerStartupConfiguration;
import org.apache.directory.server.core.configuration.MutablePartitionConfiguration;
import org.apache.directory.server.core.configuration.ShutdownConfiguration;
import org.apache.directory.server.jndi.ServerContextFactory;
import org.apache.directory.shared.ldap.exception.LdapConfigurationException;
import org.apache.directory.shared.ldap.ldif.Entry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.FileUtils;

/**
 * The Class ServletServer. Heavily based on Joakim Erfeldt's work in wagon-webdav tests.
 * 
 * @author cstamas
 */
public class LdapServer
    implements Initializable, Startable
{

    /** The Constant ROLE. */
    public static final String ROLE = LdapServer.class.getName();

    /** The working directory. */
    protected File workingDirectory;

    /** The partitions. */
    protected List<Partition> partitions;

    /** the context root for the system partition */
    protected LdapContext sysRoot;

    /** the context root for the rootDSE */
    protected LdapContext rootDSE;

    /** flag whether to delete database files for each test or not */
    protected boolean doDelete = true;

    protected MutableServerStartupConfiguration configuration = new MutableServerStartupConfiguration();

    protected int port = 1024;

    public LdapContext getSysRoot()
    {
        return sysRoot;
    }

    public void setSysRoot( LdapContext sysRoot )
    {
        this.sysRoot = sysRoot;
    }

    public LdapContext getRootDSE()
    {
        return rootDSE;
    }

    public void setRootDSE( LdapContext rootDSE )
    {
        this.rootDSE = rootDSE;
    }

    public boolean isDoDelete()
    {
        return doDelete;
    }

    public void setDoDelete( boolean doDelete )
    {
        this.doDelete = doDelete;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public List<Partition> getPartitions()
    {
        return partitions;
    }

    public void setPartitions( List<Partition> partitions )
    {
        this.partitions = partitions;
    }

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }

    public MutableServerStartupConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * Deletes the Eve working directory.
     */
    protected void doDelete( File wkdir )
        throws IOException
    {
        if ( doDelete )
        {
            if ( wkdir.exists() )
            {
                FileUtils.deleteDirectory( wkdir );
            }
            if ( wkdir.exists() )
            {
                throw new IOException( "Failed to delete: " + wkdir );
            }
        }
    }

    /**
     * Sets the contexts for this base class. Values of user and password used to set the respective JNDI properties.
     * These values can be overriden by the overrides properties.
     * 
     * @param user the username for authenticating as this user
     * @param passwd the password of the user
     * @throws NamingException if there is a failure of any kind
     */
    public void setContexts( String user, String passwd )
        throws NamingException
    {
        Hashtable<String, Object> env = new Hashtable<String, Object>( configuration.toJndiEnvironment() );
        env.put( Context.SECURITY_PRINCIPAL, user );
        env.put( Context.SECURITY_CREDENTIALS, passwd );
        env.put( Context.SECURITY_AUTHENTICATION, "simple" );
        env.put( Context.INITIAL_CONTEXT_FACTORY, ServerContextFactory.class.getName() );
        setContexts( env );
    }

    /**
     * Sets the contexts of this class taking into account the extras and overrides properties.
     * 
     * @param env an environment to use while setting up the system root.
     * @throws NamingException if there is a failure of any kind
     */
    public void setContexts( Hashtable env )
        throws NamingException
    {
        Hashtable<String, Object> envFinal = new Hashtable<String, Object>( env );
        envFinal.put( Context.PROVIDER_URL, "ou=system" );
        sysRoot = new InitialLdapContext( envFinal, null );

        envFinal.put( Context.PROVIDER_URL, "" );
        rootDSE = new InitialLdapContext( envFinal, null );
    }

    /**
     * Imports the LDIF entries packaged with the Eve JNDI provider jar into the newly created system partition to prime
     * it up for operation. Note that only ou=system entries will be added - entries for other partitions cannot be
     * imported and will blow chunks.
     * 
     * @throws NamingException if there are problems reading the ldif file and adding those entries to the system
     *         partition
     */
    public void importLdif( InputStream in )
        throws NamingException
    {
        try
        {
            Iterator<Object> iterator = new LdifReader( in );

            while ( iterator.hasNext() )
            {
                Entry entry = (Entry) iterator.next();

                LdapDN dn = new LdapDN( entry.getDn() );

                rootDSE.createSubcontext( dn, entry.getAttributes() );
            }
        }
        catch ( Exception e )
        {
            String msg = "failed while trying to parse system ldif file";
            NamingException ne = new LdapConfigurationException( msg );
            ne.setRootCause( e );
            throw ne;
        }
    }

    public DirContext createContext( String partition )
        throws NamingException
    {
        // Create a environment container
        Hashtable<Object, Object> env = new Hashtable<Object, Object>( configuration.toJndiEnvironment() );

        // Create a new context pointing to the partition
        env.put( Context.PROVIDER_URL, partition );
        env.put( Context.SECURITY_PRINCIPAL, "uid=admin,ou=system" );
        env.put( Context.SECURITY_CREDENTIALS, "secret" );
        env.put( Context.SECURITY_AUTHENTICATION, "simple" );
        env.put( Context.INITIAL_CONTEXT_FACTORY, "org.apache.directory.server.jndi.ServerContextFactory" );

        // Let's open a connection on this partition
        InitialContext initialContext = new InitialContext( env );

        // We should be able to read it
        DirContext appRoot = (DirContext) initialContext.lookup( "" );

        return appRoot;
    }

    // ===
    // Initializable iface

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable#initialize()
     */
    public void initialize()
        throws InitializationException
    {
        if ( getPartitions() != null )
        {
            Set<MutablePartitionConfiguration> pcfgs = new HashSet<MutablePartitionConfiguration>();
            for ( Partition partition : getPartitions() )
            {
                try
                {
                    // Add partition
                    MutablePartitionConfiguration pcfg = new MutablePartitionConfiguration();
                    pcfg.setName( partition.getName() );
                    pcfg.setSuffix( partition.getSuffix() );

                    // Create indices
                    if ( partition.getIndexedAttributes() != null && partition.getIndexedAttributes().size() > 0 )
                    {
                        Set<String> indexedAttrs = new HashSet<String>();
                        for ( String attr : partition.getIndexedAttributes() )
                        {
                            indexedAttrs.add( attr );
                        }
                        pcfg.setIndexedAttributes( indexedAttrs );
                    }

                    // Create a first entry associated to the partition
                    Attributes attrs = new BasicAttributes( true );

                    // First, the objectClass attribute
                    Attribute attr = new BasicAttribute( "objectClass" );
                    for ( String cls : partition.getRootEntryClasses() )
                    {
                        attr.add( cls );
                    }
                    attrs.put( attr );

                    // The default attribute
                    attr = new BasicAttribute( "o" );
                    attr.add( partition.getName() );
                    attrs.put( attr );

                    // Associate this entry to the partition
                    pcfg.setContextEntry( attrs );

                    // As we can create more than one partition, we must store
                    // each created partition in a Set before initialization
                    pcfgs.add( pcfg );

                }
                catch ( Exception e )
                {
                    throw new InitializationException( "Unable to initialize partition " + partition.getName(), e );
                }
            }
            configuration.setContextPartitionConfigurations( pcfgs );
        }

        // Create a working directory
        configuration.setWorkingDirectory( workingDirectory );
    }

    // ===
    // Startable iface

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable#start()
     */
    public void start()
        throws StartingException
    {
        try
        {
            doDelete( configuration.getWorkingDirectory() );
            // port = AvailablePortFinder.getNextAvailable( port );
            configuration.setLdapPort( getPort() );
            configuration.setShutdownHookEnabled( false );
            setContexts( "uid=admin,ou=system", "secret" );

            // load needed ldifs
            for ( Partition partition : getPartitions() )
            {
                if ( partition.getLdifFile() != null )
                {
                    importLdif( new FileInputStream( partition.getLdifFile() ) );
                }
            }

        }
        catch ( Exception e )
        {
            throw new StartingException( "Error starting embedded ApacheDS server.", e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable#stop()
     */
    public void stop()
        throws StoppingException
    {
        try
        {
            Hashtable<String, Object> env = new Hashtable<String, Object>();
            env.put( Context.PROVIDER_URL, "ou=system" );
            env.put( Context.INITIAL_CONTEXT_FACTORY, "org.apache.directory.server.jndi.ServerContextFactory" );
            env.putAll( new ShutdownConfiguration().toJndiEnvironment() );
            env.put( Context.SECURITY_PRINCIPAL, "uid=admin,ou=system" );
            env.put( Context.SECURITY_CREDENTIALS, "secret" );
            try
            {
                new InitialContext( env );
            }
            catch ( Exception e )
            {
            }

            sysRoot = null;
            doDelete( configuration.getWorkingDirectory() );
            configuration = new MutableServerStartupConfiguration();
        }
        catch ( Exception e )
        {
            throw new StoppingException( "Error stopping embedded ApacheDS server.", e );
        }
    }

    // ===
    // Private stuff

}
