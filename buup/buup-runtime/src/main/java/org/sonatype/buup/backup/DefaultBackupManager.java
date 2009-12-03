package org.sonatype.buup.backup;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.util.FileUtils;
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

    public void cleanup()
    {
        getLogger().info( "Cleaning up..." );
        
        // apply changes
        for ( OperatedFile of : getOperatedFiles().values() )
        {
            try
            {
                switch ( of.getOperation() )
                {
                    case WRITE:
                        FileUtils.rename( of.getOperatedFile(), of.getOriginalFile() );
                        break;
                    case OVERWRITE:
                        FileUtils.rename( of.getOperatedFile(), of.getOriginalFile() );
                        break;
                    case DELETE:
                        FileUtils.forceDelete( of.getOperatedFile() );
                        FileUtils.forceDelete( of.getOriginalFile() );
                        break;
                    case EDIT:
                        FileUtils.copyFile( of.getOperatedFile(), of.getOriginalFile() );
                        FileUtils.forceDelete( of.getOperatedFile() );
                        break;
                    default: // humm?
                }
            }
            catch ( IOException e )
            {
                getBuup().getLogger().error(
                    "Restore was not able to apply file operation to \"" + of.getOriginalFile() + "\"!", e );
            }
        }
        
        getLogger().info( "Cleaned up " + getOperatedFiles().size() + " count of modified files." );
    }

    public void restore()
    {
        getLogger().info( "Restoring to pre-upgrade state." );

        // delete all the .buup-* suffixed files/directories
        for ( OperatedFile of : getOperatedFiles().values() )
        {
            File dest = of.getOperatedFile();

            try
            {
                if ( dest.isFile() )
                {
                    FileUtils.forceDelete( dest );
                }
                else
                {
                    FileUtils.deleteDirectory( dest );
                }
            }
            catch ( IOException e )
            {
                getBuup().getLogger().error( "Restore was not able to delete file \"" + dest.getAbsolutePath() + "\"!",
                    e );
            }
        }

        getLogger().info( "Restored " + getOperatedFiles().size() + " count of modified files." );
    }

    protected Map<File, OperatedFile> getOperatedFiles()
    {
        if ( operatedFiles == null )
        {
            operatedFiles = new ConcurrentHashMap<File, OperatedFile>();
        }

        return operatedFiles;
    }

    protected void addOperatedFile( OperatedFile of )
    {
        getOperatedFiles().put( of.getOriginalFile(), of );
    }

    public File writeFile( File file )
        throws IOException
    {
        if ( file.exists() )
        {
            throw new IOException( "File \"" + file.getAbsolutePath() + "\" already exists!" );
        }

        File dest = new File( file.getParentFile(), file.getName() + ".buup-write" );

        addOperatedFile( new OperatedFile( Operation.WRITE, file, dest ) );

        return dest;
    }

    public File overwriteFile( File file )
        throws IOException
    {
        if ( !file.exists() )
        {
            throw new IOException( "File \"" + file.getAbsolutePath() + "\" does not exists!" );
        }

        File dest = new File( file.getParentFile(), file.getName() + ".buup-overwrite" );

        addOperatedFile( new OperatedFile( Operation.OVERWRITE, file, dest ) );

        return dest;
    }

    public File deleteFile( File file )
        throws IOException
    {
        if ( !file.exists() )
        {
            throw new IOException( "File \"" + file.getAbsolutePath() + "\" does not exists!" );
        }

        // file does not have to exist
        File dest = new File( file.getParentFile(), file.getName() + ".buup-delete" );

        if ( file.isFile() )
        {
            // "touch" dest
            FileUtils.fileWrite( dest.getAbsolutePath(), "touch me baby!" );
        }
        else if ( file.isDirectory() )
        {
            // just create empty directory to mark it's deletion
            dest.mkdir();
        }

        addOperatedFile( new OperatedFile( Operation.DELETE, file, dest ) );

        return dest;
    }

    public File editFile( File file )
        throws IOException
    {
        // file exists? if yes, copy it
        File dest = new File( file.getParentFile(), file.getName() + ".buup-edit" );

        if ( file.isFile() )
        {
            FileUtils.copyFile( file, dest );
        }
        else if ( file.exists() )
        {
            throw new IOException( "File \"" + file.getAbsolutePath() + "\" is not editable!" );
        }

        addOperatedFile( new OperatedFile( Operation.EDIT, file, dest ) );

        return dest;
    }

}
