package org.sonatype.plexus.encryptor.cli;

import java.io.PrintStream;

public class PlexusEncryptorCliTest
    extends AbstractPlexusEncryptorCliTest
{

    protected PlexusEncryptorCli cli;

    private String originalUserDir;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        cli = new PlexusEncryptorCli();

        System.setOut( new PrintStream( out ) );
        System.setErr( new PrintStream( out ) );

        originalUserDir = System.getProperty( "user.dir" );
        System.setProperty( "user.dir", DEST_DIR.getAbsolutePath() );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        System.setProperty( "user.dir", originalUserDir );
    }

    @Override
    protected int execute( String... args )
    {
        return cli.execute( args );
    }

}
