package org.sonatype.buup.actions;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

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

    /**
     * Copied a file and tries to figure out where to copy it.
     */
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

    /**
     * Tries to figure out what to delete.
     * 
     * @param destination
     * @param failIfNotFound
     * @throws IOException
     */
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

    /**
     * Return true if any of the passed files exists.
     * 
     * @param files
     * @param all to check for existence ALL (true) or ANY (false)
     * @return true if any of passed files exists.
     */
    protected boolean filesExists( final Collection<File> files, final boolean all )
    {
        for ( File file : files )
        {
            if ( !all && file.exists() )
            {
                return true;
            }
            else if ( all && !file.exists() )
            {
                return false;
            }
        }

        return all;
    }

    protected File resolveChildPath( File basedir, String childPath )
    {
        File child = new File( childPath );

        if ( child.isAbsolute() )
        {
            return child;
        }
        else
        {
            return new File( basedir, childPath );
        }
    }
}
