package org.sonatype.buup.actions;

import java.io.File;
import java.io.IOException;

public class RemoveJarAction
    implements Action
{
    private File jarToRemove;

    private boolean failIfNotExists;

    public void perform()
        throws Exception
    {
        if ( !jarToRemove.isFile() && failIfNotExists )
        {
            throw new IOException( "JAR does not exists on path \"" + jarToRemove.getAbsolutePath() + "\"!" );
        }

        if ( !jarToRemove.delete() )
        {

        }
    }

}
