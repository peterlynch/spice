package org.sonatype.buup.backup;

import java.io.IOException;

import org.sonatype.buup.recipe.Recipe;

public interface BackupManager
{
    void backup( Recipe recipe )
        throws IOException;

    void restore( Recipe recipe )
        throws IOException;
}
