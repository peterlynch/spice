
package org.codehaus.plexus.build.incremental;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;

/**
 * Filesystem based build context implementation which behaves as if all files
 * were just created. More specifically, 
 * 
 * hasDelta returns <code>true</code> for all paths
 * newScanner returns Scanner that scanns all files under provided basedir
 * newDeletedScanner always returns empty scanner.
 * 
 * @plexus.component role="org.codehaus.plexus.build.incremental.BuildContext"
 *                   role-hint="default"
 */
public class DefaultBuildContext implements BuildContext {

  public boolean hasDelta(String relPath) {
    return true;
  }

  public boolean hasDelta(List relPaths) {
    return true;
  }

  public OutputStream newFileOutputStream(File file) throws IOException {
    return new FileOutputStream(file);
  }

  public Scanner newScanner(File basedir) {
    DirectoryScanner ds = new DirectoryScanner();
    ds.setBasedir(basedir);
    return ds;
  }

  public void refresh(File file) {
    // do nothing
  }

  public Scanner newDeleteScanner(File basedir) {
    return EmptyScanner.INSTANCE;
  }

  public Scanner newScanner(File basedir, boolean ignoreDelta) {
    return newScanner(basedir);
  }

}
