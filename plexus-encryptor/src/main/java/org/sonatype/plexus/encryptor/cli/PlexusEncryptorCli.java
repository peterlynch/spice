package org.sonatype.plexus.encryptor.cli;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.tools.cli.AbstractCli;
import org.sonatype.plexus.encryptor.PlexusEncryptor;

public class PlexusEncryptorCli
    extends AbstractCli
{

    private static final char GENERATE = 'g';

    private static final char ENCRYPT = 'e';

    private static final char DECRYPT = 'd';

    private static final char PUBLIC_KEY = 'p';

    private static final char PRIVATE_KEY = 'k';

    private static final char INPUT = 'i';

    private static final char OUTPUT = 'o';

    public static void main( String[] args )
    {
        int result = new PlexusEncryptorCli().execute( args );
        if ( result != 0 )
        {
            System.exit( result );
        }
    }

    private Options options;

    private int status;

    @Override
    public int execute( String[] arg0, ClassWorld arg1 )
    {
        int value = super.execute( arg0, arg1 );

        if ( status == 0 )
        {
            status = value;
        }

        return status;
    }

    @SuppressWarnings( "static-access" )
    @Override
    public Options buildCliOptions( Options options )
    {
        this.options = options;

        options.addOption( OptionBuilder.withLongOpt( "generate" ) //
        .withDescription( "Generate encryption keys" ).create( GENERATE ) );

        options.addOption( OptionBuilder.withLongOpt( "encrypt" ) //
        .withDescription( "Encrypt the source file using the public key" ).create( ENCRYPT ) );

        options.addOption( OptionBuilder.withLongOpt( "decrypt" ) //
        .withDescription( "Decrypt the source file using the private key" ).create( DECRYPT ) );

        options.addOption( OptionBuilder.withLongOpt( "publicKey" ).hasArg() //
        .withDescription( "Public encryption key" ).create( PUBLIC_KEY ) );

        options.addOption( OptionBuilder.withLongOpt( "secretKey" ).hasArg() //
        .withDescription( "Private encryption key" ).create( PRIVATE_KEY ) );

        options.addOption( OptionBuilder.withLongOpt( "input" ).hasArg() //
        .withDescription( "Source file to be encrypted/decrypted" ).create( INPUT ) );

        options.addOption( OptionBuilder.withLongOpt( "output" ).hasArg() //
        .withDescription( "Result file after encryption/decryption" ).create( OUTPUT ) );

        return options;
    }

    @Override
    public void invokePlexusComponent( CommandLine cli, PlexusContainer plexus )
        throws Exception
    {
        if ( cli.hasOption( QUIET ) )
        {
            setLogLevel( plexus, Logger.LEVEL_DISABLED );
        }
        else if ( cli.hasOption( DEBUG ) )
        {
            setLogLevel( plexus, Logger.LEVEL_DEBUG );
        }
        else if ( cli.hasOption( ERRORS ) )
        {
            setLogLevel( plexus, Logger.LEVEL_ERROR );
        }

        PlexusEncryptor encryptor = plexus.lookup( PlexusEncryptor.class, "rsa-aes" );

        try
        {
            if ( cli.hasOption( GENERATE ) )
            {
                generate( cli, encryptor );
            }
            else if ( cli.hasOption( ENCRYPT ) )
            {
                encrypt( cli, encryptor );
            }
            else if ( cli.hasOption( DECRYPT ) )
            {
                decrypt( cli, encryptor );
            }
            else
            {
                status = 1;
                displayHelp();
            }
        }
        catch ( Exception e )
        {
            status = 1;
            throw e;
        }

    }

    private void decrypt( CommandLine cli, PlexusEncryptor encryptor )
        throws GeneralSecurityException, IOException
    {
        File privateKey = getPrivateKey( cli );
        if ( !privateKey.exists() )
        {
            throw new IllegalArgumentException( "Private key not found at: " + privateKey );
        }

        File source = getSourceFile( cli );
        File destination = getDestinationFile( cli, "dec" );

        encryptor.decrypt( source, destination, privateKey );
    }

    private void encrypt( CommandLine cli, PlexusEncryptor encryptor )
        throws GeneralSecurityException, IOException
    {
        File publicKey = getPublicKey( cli );
        if ( !publicKey.exists() )
        {
            throw new IllegalArgumentException( "Public key not found at: " + publicKey );
        }

        File source = getSourceFile( cli );
        File destination = getDestinationFile( cli, "enc" );

        encryptor.encrypt( source, destination, publicKey );
    }

    private File getDestinationFile( CommandLine cli, String suffix )
    {
        File destination = getFile( cli, OUTPUT, null );
        if ( destination == null )
        {
            destination = new File( getSourceFile( cli ).getAbsolutePath() + "." + suffix );
        }
        return destination;
    }

    private File getSourceFile( CommandLine cli )
    {
        File sourceFile = getFile( cli, INPUT, null );
        if ( sourceFile == null )
        {
            throw new IllegalArgumentException( "Source file not specified." );
        }
        return sourceFile;
    }

    private void generate( CommandLine cli, PlexusEncryptor encryptor )
        throws IOException, GeneralSecurityException
    {
        File publicKey = getPublicKey( cli );
        File privateKey = getPrivateKey( cli );

        encryptor.generateKeys( publicKey, privateKey );
    }

    private File getPrivateKey( CommandLine cli )
    {
        return getFile( cli, PRIVATE_KEY, "private.key" );
    }

    private File getPublicKey( CommandLine cli )
    {
        return getFile( cli, PUBLIC_KEY, "public.key" );
    }

    private File getFile( CommandLine cli, char optionId, String defaultName )
    {
        String pathPath = cli.getOptionValue( optionId );

        File key;
        if ( pathPath != null )
        {
            key = new File( pathPath );
        }
        else
        {
            if ( defaultName != null )
            {
                key = new File( defaultName );
            }
            else
            {
                return null;
            }
        }

        try
        {
            return key.getCanonicalFile();
        }
        catch ( IOException e )
        {
            return key.getAbsoluteFile();
        }
    }

    private void setLogLevel( PlexusContainer plexus, int logLevel )
        throws ComponentLookupException
    {
        plexus.lookup( LoggerManager.class ).setThreshold( logLevel );
    }

    @Override
    public void displayHelp()
    {
        System.out.println();

        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp( "usage: java -jar plexus-encryptor [options]", "\nOptions:", options, "\n" );
    }

}
