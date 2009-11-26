package org.sonatype.buup.backup;

import java.io.IOException;

import org.sonatype.buup.Buup;

public interface BackupManager
{
    void backup( Buup buup )
        throws IOException;

    void restore( Buup buup )
        throws IOException;
}
