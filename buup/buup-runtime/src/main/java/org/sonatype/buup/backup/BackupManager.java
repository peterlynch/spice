package org.sonatype.buup.backup;

import java.io.IOException;

public interface BackupManager
{
    void backup()
        throws IOException;

    void restore()
        throws IOException;
}
