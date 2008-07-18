package org.sonatype.appbooter;

import java.io.File;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
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
        AbstractForkedAppBooter appBooter = (AbstractForkedAppBooter) this.lookup( ForkedAppBooter.ROLE );

        File platformFile = appBooter.getPlatformFile();

        Assert.assertTrue( "my-nonexisting-platform.jar".equals( platformFile.getName() ) );
    }

    public void testBuildCommandLine()
        throws Exception
    {
        AbstractForkedAppBooter appBooter = (AbstractForkedAppBooter) this.lookup( ForkedAppBooter.ROLE );

        Commandline cmd = appBooter.buildCommandLine();

        // this is a very very week test, it doesn't test anything
        Assert.assertTrue( cmd.toString().endsWith( appBooter.getPlatformFile().getAbsolutePath() ) );
    }

    public void testRunAppBooter()
        throws Exception
    {
        ForkedAppBooter appBooter = (ForkedAppBooter) this.lookup( ForkedAppBooter.ROLE );

        // to test this we would need a component to run that just waits.
        // appBooter.runAppBooter();
    }

}
