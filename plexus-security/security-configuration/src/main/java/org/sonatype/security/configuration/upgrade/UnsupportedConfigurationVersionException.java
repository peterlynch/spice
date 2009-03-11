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
package org.sonatype.security.configuration.upgrade;

import java.io.File;

import org.sonatype.security.configuration.ConfigurationException;

/**
 * Thrown when the configuration has model version but it is unknown to Nexus.
 * 
 * @author cstamas
 */
public class UnsupportedConfigurationVersionException
    extends ConfigurationException
{
    private static final long serialVersionUID = 1965812260368747123L;

    public UnsupportedConfigurationVersionException( String version, File file )
    {
        super( "Unsupported configuration file in " + file.getAbsolutePath() + " with version: " + version
            + ". Cannot upgrade." );
    }
}
