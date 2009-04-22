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
package org.sonatype.security.configuration.upgrade;

import java.io.File;
import java.io.StringWriter;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.security.configuration.AbstractSecurityConfigTest;
import org.sonatype.security.configuration.upgrade.SecurityConfigurationUpgrader;

public class DefaultSecurityConfigurationUpgraderTest
    extends AbstractSecurityConfigTest
{

    protected SecurityConfigurationUpgrader configurationUpgrader;

    public void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.cleanDirectory( new File( getSecurityConfiguration() ).getParentFile() );

        this.configurationUpgrader = (SecurityConfigurationUpgrader) lookup( SecurityConfigurationUpgrader.class );
    }

    protected void resultIsFine( String path, Configuration configuration )
        throws Exception
    {
        SecurityConfigurationXpp3Writer w = new SecurityConfigurationXpp3Writer();

        StringWriter sw = new StringWriter();

        w.write( sw, configuration );

        String shouldBe = IOUtil.toString( getClass().getResourceAsStream( path + ".result" ) );

        assertEquals( shouldBe, sw.toString() );
    }

    public void testFrom100()
        throws Exception
    {
        copyFromClasspathToFile(
            "/org/sonatype/security/configuration/upgrade/security-100.xml",
            getSecurityConfiguration() );

        Configuration configuration = configurationUpgrader
            .loadOldConfiguration( new File( getSecurityConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        resultIsFine( "/org/sonatype/security/configuration/upgrade/security-100.xml", configuration );
    }

    public void testFrom100Part2()
        throws Exception
    {
        copyFromClasspathToFile(
            "/org/sonatype/security/configuration/upgrade/security-100-2.xml",
            getSecurityConfiguration() );

        Configuration configuration = configurationUpgrader
            .loadOldConfiguration( new File( getSecurityConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        resultIsFine( "/org/sonatype/security/configuration/upgrade/security-100-2.xml", configuration );
    }

    public void testFrom201to202()
        throws Exception
    {
        copyFromClasspathToFile(
            "/org/sonatype/security/configuration/upgrade/security-100-2.xml",
            getSecurityConfiguration() );

        Configuration configuration = configurationUpgrader
            .loadOldConfiguration( new File( getSecurityConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        resultIsFine( "/org/sonatype/security/configuration/upgrade/security-100-2.xml", configuration );
    }
}
