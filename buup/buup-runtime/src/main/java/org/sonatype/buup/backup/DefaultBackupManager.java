package org.sonatype.buup.backup;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.buup.Buup;

public class DefaultBackupManager
    implements BackupManager
{
    private Logger logger = LoggerFactory.getLogger( getClass() );

    public Logger getLogger()
    {
        return logger;
    }

    private final Buup buup;

    private Map<File, OperatedFile> operatedFiles;

    public DefaultBackupManager( Buup buup )
    {
        this.buup = buup;
    }

    protected Buup getBuup()
    {
        return buup;
    }

    public void backup()
        throws IOException
    {
        // erm?
    }

    public void commit()
        throws IOException
    {
        getLogger().info( "Committing changes..." );

        // apply changes
        for ( OperatedFile of : getOperatedFiles().values() )
        {
            try
            {
                of.commit();
            }
            catch ( IOException e )
            {
                getBuup().getLogger().error(
                    "Commit was not able to apply file operation to \"" + of.getOriginalFile() + "\"!", e );

                throw e;
            }
        }

        cleanup( false );

        getLogger().info( "Committed " + getOperatedFiles().size() + " modified files." );
    }

    public void rollback()
    {
        getLogger().info( "Rolling back changes..." );

        // delete all the .buup-* suffixed files/directories
        for ( OperatedFile of : getOperatedFiles().values() )
        {
            try
            {
                of.rollback();
            }
            catch ( IOException e )
            {
                getBuup().getLogger().error(
                    "Restore was not able to restore file \"" + of.getOriginalFile().getAbsolutePath() + "\"!", e );
            }
        }

        try
        {
            cleanup( true );
        }
        catch ( IOException e )
        {
            // will not happen, called with "true"
        }

        getLogger().info( "Rolled-back " + getOperatedFiles().size() + " modified files." );
    }

    protected void cleanup( boolean force )
        throws IOException
    {
        getLogger().info( "Clean-up invoked..." );

        // delete all the .buup-* suffixed files/directories
        for ( OperatedFile of : getOperatedFiles().values() )
        {
            try
            {
                of.cleanup( force );
            }
            catch ( IOException e )
            {
                getBuup().getLogger()
                    .error(
                        "Clean-up was not able to perform against file \"" + of.getOriginalFile().getAbsolutePath()
                            + "\"!", e );

                if ( !force )
                {
                    throw e;
                }
            }
        }

        getLogger().info( "Cleaned up " + getOperatedFiles().size() + " modified files." );
    }

    protected Map<File, OperatedFile> getOperatedFiles()
    {
        if ( operatedFiles == null )
        {
            operatedFiles = new ConcurrentHashMap<File, OperatedFile>();
        }

        return operatedFiles;
    }

    protected OperatedFile addOperatedFile( Operation op, File file )
        throws IOException
    {
        OperatedFile of = new OperatedFile( op, file );

        getOperatedFiles().put( of.getOriginalFile(), of );

        return of;
    }

    public File writeFile( File file )
        throws IOException
    {
        return addOperatedFile( Operation.WRITE, file ).getOperatedFile();
    }

    public File overwriteFile( File file )
        throws IOException
    {
        return addOperatedFile( Operation.OVERWRITE, file ).getOperatedFile();
    }

    public File deleteFile( File file )
        throws IOException
    {
        return addOperatedFile( Operation.DELETE, file ).getOperatedFile();
    }

    public File editFile( File file )
        throws IOException
    {
        return addOperatedFile( Operation.EDIT, file ).getOperatedFile();
    }

}
