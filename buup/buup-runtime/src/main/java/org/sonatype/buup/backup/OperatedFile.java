package org.sonatype.buup.backup;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;

public class OperatedFile
{
    private final File originalFile;

    private final File backupFile;

    private final File operatedFile;

    private final Operation operation;

    public OperatedFile( Operation oper, File originalFile )
        throws IOException
    {
        this.operation = oper;

        this.originalFile = originalFile;

        this.backupFile = backupFile( originalFile );

        switch ( getOperation() )
        {
            case WRITE:
                if ( originalFile.exists() )
                {
                    throw new IOException( "File \"" + originalFile.getAbsolutePath() + "\" already exists!" );
                }

                operatedFile = new File( originalFile.getParentFile(), originalFile.getName() + ".buup-write" );

                break;

            case OVERWRITE:
                if ( !originalFile.exists() )
                {
                    throw new IOException( "File \"" + originalFile.getAbsolutePath() + "\" does not exists!" );
                }

                operatedFile = new File( originalFile.getParentFile(), originalFile.getName() + ".buup-overwrite" );

                break;

            case DELETE:
                if ( !originalFile.exists() )
                {
                    throw new IOException( "File \"" + originalFile.getAbsolutePath() + "\" does not exists!" );
                }

                // file does not have to exist
                operatedFile = new File( originalFile.getParentFile(), originalFile.getName() + ".buup-delete" );

                if ( originalFile.isFile() )
                {
                    // "touch" dest
                    FileUtils.fileWrite( operatedFile.getAbsolutePath(), "touch me baby!" );
                }
                else if ( originalFile.isDirectory() )
                {
                    // just create empty directory to mark it's deletion
                    operatedFile.mkdir();
                }

                break;

            case EDIT:
                // file exists? if yes, copy it
                operatedFile = new File( originalFile.getParentFile(), originalFile.getName() + ".buup-edit" );

                if ( originalFile.isFile() )
                {
                    FileUtils.copyFile( originalFile, operatedFile );
                }
                else if ( originalFile.exists() )
                {
                    throw new IOException( "File \"" + originalFile.getAbsolutePath() + "\" is not editable!" );
                }

                break;

            default:
                throw new IOException( "Operation \"" + String.valueOf( oper ) + "\" is unknown!" );
        }
    }

    protected File backupFile( File file )
        throws IOException
    {
        File bf = new File( originalFile.getParentFile(), originalFile.getName() + ".buup-bak" );

        if ( file.isFile() )
        {
            FileUtils.copyFile( file, bf );
        }
        else if ( file.isDirectory() )
        {
            FileUtils.copyDirectoryStructure( file, bf );
        }

        return bf;
    }

    public Operation getOperation()
    {
        return operation;
    }

    public File getOriginalFile()
    {
        return originalFile;
    }

    public File getBackupFile()
    {
        return backupFile;
    }

    public File getOperatedFile()
    {
        return operatedFile;
    }

    public void commit()
        throws IOException
    {
        switch ( getOperation() )
        {
            case WRITE:
            case OVERWRITE:
            case EDIT:
                FileUtils.copyFile( getOperatedFile(), getOriginalFile() );
                break;
            case DELETE:
                FileUtils.forceDelete( getOriginalFile() );
                break;
            default: // humm?
        }
    }

    public void rollback()
        throws IOException
    {
        // undo deletion only if needed
        if ( Operation.DELETE.equals( getOperation() ) && !getOriginalFile().exists() )
        {
            if ( getBackupFile().isFile() )
            {
                // undo write/overwrite/edit
                FileUtils.copyFile( getBackupFile(), getOriginalFile() );
            }
            else if ( getBackupFile().isDirectory() )
            {
                FileUtils.copyDirectoryStructure( getBackupFile(), getOriginalFile() );
            }
        }
        else
        {
            // do it if we have backup (we don't have on WRITE operation for example)
            if ( getBackupFile().isFile() )
            {
                // undo write/overwrite/edit
                FileUtils.copyFile( getBackupFile(), getOriginalFile() );
            }
            else if ( getBackupFile().isDirectory() )
            {
                FileUtils.copyDirectoryStructure( getBackupFile(), getOriginalFile() );
            }
        }
    }

    public void cleanup( boolean force )
        throws IOException
    {
        IOException eToThrow = null;

        try
        {
            if ( getOperatedFile().isFile() )
            {
                FileUtils.forceDelete( getOperatedFile() );
            }
            else
            {
                FileUtils.deleteDirectory( getOperatedFile() );
            }
        }
        catch ( IOException e )
        {
            eToThrow = e;

            if ( !force )
            {
                throw e;
            }
        }

        if ( getBackupFile().isFile() )
        {
            FileUtils.forceDelete( getBackupFile() );
        }
        else
        {
            FileUtils.deleteDirectory( getBackupFile() );
        }

        if ( eToThrow != null )
        {
            throw eToThrow;
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( operation == null ) ? 0 : operation.hashCode() );
        result = prime * result + ( ( originalFile == null ) ? 0 : originalFile.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        OperatedFile other = (OperatedFile) obj;
        if ( operation == null )
        {
            if ( other.operation != null )
                return false;
        }
        else if ( !operation.equals( other.operation ) )
            return false;
        if ( originalFile == null )
        {
            if ( other.originalFile != null )
                return false;
        }
        else if ( !originalFile.equals( other.originalFile ) )
            return false;
        return true;
    }
}
