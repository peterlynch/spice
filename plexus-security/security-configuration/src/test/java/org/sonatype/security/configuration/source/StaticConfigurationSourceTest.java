/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.configuration.source;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.security.configuration.source.SecurityConfigurationSource;

public class StaticConfigurationSourceTest
    extends AbstractSecurityConfigurationSourceTest
{

    @Override
    protected SecurityConfigurationSource getConfigurationSource()
        throws Exception
    {
        return ( SecurityConfigurationSource ) lookup( SecurityConfigurationSource.class, "static" );
    }

    @Override
    protected InputStream getOriginatingConfigurationInputStream()
        throws IOException
    {
        return getClass().getResourceAsStream( "/META-INF/security/security.xml" );
    }

    public void testStoreConfiguration()
        throws Exception
    {
        configurationSource = getConfigurationSource();

        configurationSource.loadConfiguration();

        try
        {
            configurationSource.storeConfiguration();

            fail();
        }
        catch ( UnsupportedOperationException e )
        {
            // good
        }
    }

    public void testIsConfigurationUpgraded()
        throws Exception
    {
        configurationSource = getConfigurationSource();

        configurationSource.loadConfiguration();

        assertEquals( false, configurationSource.isConfigurationUpgraded() );
    }

    public void testIsConfigurationDefaulted()
        throws Exception
    {
        configurationSource = getConfigurationSource();

        configurationSource.loadConfiguration();

        assertEquals( false, configurationSource.isConfigurationDefaulted() );
    }

    public void testGetDefaultsSource()
        throws Exception
    {
        configurationSource = getConfigurationSource();

        assertEquals( null, configurationSource.getDefaultsSource() );
    }

}
