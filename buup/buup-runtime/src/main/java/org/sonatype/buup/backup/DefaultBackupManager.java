package org.sonatype.buup.backup;

import java.io.IOException;

import org.sonatype.buup.Buup;

public class DefaultBackupManager
    implements BackupManager
{
    private final Buup buup;

    public DefaultBackupManager( Buup buup )
    {
        this.buup = buup;
    }

    public void backup()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    public void restore()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

}
