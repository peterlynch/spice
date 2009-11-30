package org.sonatype.buup.actions;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.buup.Buup;

public abstract class AbstractFileManipulatorAction
    extends AbstractAction
{
    public AbstractFileManipulatorAction( Buup buup )
    {
        super( buup );
    }

    // ==

    protected void copyFile( File source, File destination, boolean overwrite )
        throws IOException
    {
        if ( source == null || !source.isFile() )
        {
            throw ioexception( "Illegal copy operation, source is not a file!" );
        }
        if ( destination == null )
        {
            throw ioexception( "Illegal copy operation, destination is null!" );
        }

        if ( destination.isDirectory() )
        {
            // check is destination there
            File target = new File( destination, source.getName() );

            if ( target.isFile() && overwrite )
            {
                FileUtils.copyFileToDirectory( source, destination );
            }
            else if ( !target.exists() )
            {
                FileUtils.copyFileToDirectory( source, destination );
            }
            else
            {
                throw ioexception( "Target file already exists (and is not a file or no overwrite is given)!" );
            }
        }
        else
        {
            if ( destination.isFile() && overwrite )
            {
                FileUtils.copyFileToDirectory( source, destination );
            }
            else if ( !destination.exists() )
            {
                FileUtils.copyFileToDirectory( source, destination );
            }
            else
            {
                throw ioexception( "Target file already exists (and is not a file or no overwrite is given)!" );
            }
        }
    }

    protected void deleteFile( File destination, boolean failIfNotFound )
        throws IOException
    {
        if ( destination.isFile() )
        {
            FileUtils.forceDelete( destination );
        }
        else if ( destination.isDirectory() )
        {
            FileUtils.deleteDirectory( destination );
        }
        else if ( failIfNotFound )
        {
            throw ioexception( "Illegal delete operation, file to delete does not exists!" );
        }

    }

}
