package org.sonatype.plexus.encryptor.cli;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class PlexusEncryptorCliIT
    extends AbstractPlexusEncryptorCliTest
{

    private StreamConsumer sout;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        sout = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                try
                {
                    out.write( line.getBytes() );
                    out.write( "\n".getBytes() );
                }
                catch( IOException e )
                {
                    throw new RuntimeException( e.getMessage(), e );
                }
            }
        };
    }

    private Commandline createCommandLine()
    {
        try
        {
            Commandline cmd = new Commandline();
            cmd.setExecutable( "java" );
            cmd.setWorkingDirectory( DEST_DIR );
            cmd.createArg().setValue( "-jar" );
            cmd.createArg().setValue( new File( System.getProperty( "encryptorJar" ) ).getCanonicalPath() );
            return cmd;
        }
        catch( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    protected int execute( String... args )
    {
        Commandline cmd = createCommandLine();
        for( String arg : args )
        {
            cmd.createArg().setValue( arg );
        }
        try
        {
            return CommandLineUtils.executeCommandLine( cmd, sout, sout );
        }
        catch( CommandLineException e )
        {
            return -1;
        }
    }

}
