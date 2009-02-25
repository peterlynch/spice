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
        ldapServer.stop();
        ldapServer = null;
     
        super.tearDown();
    }
}
