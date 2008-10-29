/*
 * Copyright 2005.-2007. Tamas Cservenak (t.cservenak@gmail.com)
 * 
 */
package org.sonatype.ldaptestsuite;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.sonatype.ldaptestsuite.LdapServer;

public abstract class AbstractLdapTestEnvironment
    extends PlexusTestCase
{
    /** The logger. */
    private Logger logger;

    /** The ldap server. */
    private LdapServer ldapServer;

    /**
     * Gets the logger.
     * 
     * @return the logger
     */
    public Logger getLogger()
    {
        return logger;
    }

    /**
     * Gets the ldap server.
     * 
     * @return the ldap server
     */
    public LdapServer getLdapServer()
    {
        return ldapServer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.plexus.PlexusTestCase#setUp()
     */
    public void setUp()
        throws Exception
    {
        super.setUp();

        LoggerManager loggerManager = (LoggerManager) lookup( LoggerManager.ROLE );

        logger = loggerManager.getLoggerForComponent( this.getClass().toString() );

        ldapServer = (LdapServer) lookup( LdapServer.ROLE );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.plexus.PlexusTestCase#tearDown()
     */
    public void tearDown()
        throws Exception
    {
        super.tearDown();
        
        ldapServer.stop();
    }
}
