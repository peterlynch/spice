/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.appbooter;

import java.io.File;
import java.lang.reflect.Field;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.cli.Commandline;
import org.sonatype.plexus.classworlds.model.ClassworldsRealmConfiguration;

public class DefaultForkedAppBooterTest
    extends PlexusTestCase
{

    public void testConfig()
        throws Exception
    {
        Object object = this.lookup( ForkedAppBooter.ROLE );
        assertTrue( object instanceof ForkedAppBooter );
    }

    public void testGetClassworldsRealmConfig()
        throws Exception
    {
        AbstractForkedAppBooter appBooter = (AbstractForkedAppBooter) this.lookup( ForkedAppBooter.ROLE );

        ClassworldsRealmConfiguration classworldsConfig = appBooter.getClassworldsRealmConfig();

        Assert.assertTrue( classworldsConfig.getLoadPatterns().size() == 2 );

    }

    public void testGetPlatformFile()
        throws Exception
    {
        AbstractForkedAppBooter appBooter = (AbstractForkedAppBooter) this.lookup( ForkedAppBooter.ROLE, "default" );

        File platformFile = appBooter.getPlatformFile();

        Assert.assertTrue( "my-nonexisting-platform.jar".equals( platformFile.getName() ) );
    }

    public void testConfigurator()
        throws Exception
    {
        ForkedAppBooter appBooter = (ForkedAppBooter) this.lookup( ForkedAppBooter.ROLE, "default" );

        ComponentDescriptor descript = this.getContainer().getComponentDescriptor( ForkedAppBooter.ROLE, "default" );
        PlexusConfiguration config = descript.getConfiguration();

        // get the value and set it to 25
        PlexusConfiguration[] wow = config.getChildren( "control-port" ); // the private controlPort field
        wow[0].setValue( "25" );

        // get the configurator:
        ComponentConfigurator configurator = (ComponentConfigurator) this.lookup( ComponentConfigurator.ROLE, "basic" );
        // configure the component
        configurator.configureComponent( appBooter, config,
                                         this.getContainer().getComponentRealm( ForkedAppBooter.ROLE ) );

        // now just test the value for kicks
        Field contorlPortField = AbstractForkedAppBooter.class.getDeclaredField( "controlPort" );
        contorlPortField.setAccessible( true );

        int controlPort = contorlPortField.getInt( appBooter );
        Assert.assertEquals( 25, controlPort );

    }

    public void testBuildCommandLine()
        throws Exception
    {
        AbstractForkedAppBooter appBooter = (AbstractForkedAppBooter) this.lookup( ForkedAppBooter.ROLE );

        Commandline cmd = appBooter.buildCommandLine();

        System.out.println( "cmd: " + cmd );

        String platformFilePath = appBooter.getPlatformFile().getAbsolutePath();
        // this is a very very week test, it doesn't test anything
        Assert.assertTrue( cmd.toString().endsWith( platformFilePath )// my machine
            || cmd.toString().endsWith( platformFilePath + "\"" ) // maybe windows
            || cmd.toString().endsWith( platformFilePath + "\"\"" ) // windows
            || cmd.toString().endsWith( platformFilePath + "'" ) ); // unix

    }

    public void testBuildCommandLineWithAsterisk()
        throws Exception
    {
        AbstractForkedAppBooter appBooter =
            (AbstractForkedAppBooter) this.lookup( ForkedAppBooter.ROLE, "withAsterisk" );

        Commandline cmd = appBooter.buildCommandLine();
        
        System.out.println( "cmd: " + cmd );

        String platformFilePath = appBooter.getPlatformFile().getAbsolutePath();
        
        Assert.assertFalse( platformFilePath.contains( "*" ) );
        
        // this is a very very week test, it doesn't test anything
        Assert.assertTrue( cmd.toString().endsWith( platformFilePath ) // my machine
                           || cmd.toString().endsWith( platformFilePath + "\"" ) // maybe windows
                           || cmd.toString().endsWith( platformFilePath + "\"\"" ) // windows
                           || cmd.toString().endsWith( platformFilePath + "'" ) ); // unix
        
    }

    public void testRunAppBooter()
        throws Exception
    {
        ForkedAppBooter appBooter = (ForkedAppBooter) this.lookup( ForkedAppBooter.ROLE );

        // to test this we would need a component to run that just waits.
        // appBooter.runAppBooter();
    }

}
