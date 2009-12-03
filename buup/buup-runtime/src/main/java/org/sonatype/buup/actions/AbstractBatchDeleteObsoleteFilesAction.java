package org.sonatype.buup.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBatchDeleteObsoleteFilesAction
    extends AbstractFileManipulatorAction
{
    /**
     * The basedir from where to resolve relative files (if any found in the list to be deleted).
     */
    private File targetDir;

    /**
     * A flag that will make action fail if not all files to be deleted is found.
     */
    private boolean allShouldExists = true;

    public File getTargetDir()
    {
        return targetDir;
    }

    public void setTargetDir( File targetDir )
    {
        this.targetDir = targetDir;
    }

    public boolean isAllShouldExists()
    {
        return allShouldExists;
    }

    public void setAllShouldExists( boolean allShouldExists )
    {
        this.allShouldExists = allShouldExists;
    }

    public void perform( ActionContext ctx )
        throws Exception
    {
        List<String> filePathsToDelete = getFilePathsToDelete();

        ArrayList<File> filesToDelete = new ArrayList<File>( filePathsToDelete.size() );

        // resolve relative files
        for ( String filepathToDelete : filePathsToDelete )
        {
            filesToDelete.add( resolveChildPath( getTargetDir(), filepathToDelete ) );
        }

        boolean existsAllOrSome = filesExists( filesToDelete, isAllShouldExists() );

        if ( ( isAllShouldExists() && existsAllOrSome ) || !isAllShouldExists() )
        {
            for ( File fileToDelete : filesToDelete )
            {
                deleteFile( ctx, fileToDelete, isAllShouldExists() );
            }
        }
        else
        {
            throw ioexception( "Not all files to be deleted exists! Is this a tampered Nexus instance? Not upgrading..." );
        }
    }

    protected abstract List<String> getFilePathsToDelete();
}
