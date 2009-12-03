package org.sonatype.buup.backup;

import java.io.File;
import java.io.IOException;

public interface BackupManager
{
    /**
     * Backs up files for any potential restore call.
     * 
     * @throws IOException
     */
    void backup()
        throws IOException;

    /**
     * Performs a full restore, putting back the backed up files to their place.
     */
    void restore();

    /**
     * Removes the backup files (to be called after sucessful upgrade).
     */
    void cleanup();

    // ==

    // Write
    // * File must not exists
    // * return file modified (buup-write)
    //
    // Overwrite
    // * File must exists
    // * return file modified (buup-overwrite)
    //
    // Delete
    // * File must exists
    // * create modified file
    // * return file modified (buup-delete)
    //
    // Edit
    // * File existence is not enforced, but
    // * if target file exists, it is copied to renamed file
    // * return file modified (buup-edit)
    //
    //
    // On restore:
    // delete all files ending with (buup-*)
    //
    // On cleanup
    // buup-write -> rename it
    // buup-overwrite -> delete target, rename it
    // buup-delete -> delete both
    // buup-edit -> same as overwrite or write

    File writeFile( File file )
        throws IOException;

    File overwriteFile( File file )
        throws IOException;

    File deleteFile( File file )
        throws IOException;

    File editFile( File file )
        throws IOException;
}
