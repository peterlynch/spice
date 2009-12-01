package org.sonatype.buup.actions;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

public class CheckReadWritePermissionsAction
    extends AbstractAction
{
    protected static final String TEST_CONTENT = "buup FS perms test";

    public void perform( ActionContext ctx )
        throws IOException
    {
        checkRWAccess( ctx.getBasedir() );
    }

    // ==

    protected void checkRWAccess( File directory )
        throws IOException
    {
        if ( !directory.isDirectory() )
        {
            throw new IOException( "The path \"" + directory.getAbsolutePath()
                + "\" does not points to existing directory! " );
        }

        // try to write
        File tmpFile = new File( directory, "buup-write-test.txt" );

        FileUtils.fileWrite( tmpFile.getAbsolutePath(), TEST_CONTENT );

        // try to read
        String content = FileUtils.fileRead( tmpFile );

        if ( !StringUtils.equals( TEST_CONTENT, content ) )
        {
            throw new IOException( "Cannot read or read is incomplete of file \"" + tmpFile.getAbsolutePath() + "\"!" );
        }

        // clean up
        FileUtils.forceDelete( tmpFile );
    }
}
