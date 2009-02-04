
package org.codehaus.plexus.build.incremental;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.codehaus.plexus.util.Scanner;


// TODO should it be BuildWorkspace or something like that?
public interface BuildContext {

  // TODO should we add File getBasedir()?
  
  /**
   * Returns <code>true</code> if file or folder identified by <code>relpath</code> has 
   * changed since last build. 
   * 
   * @param relpath is path relative to build context basedir
   */
  boolean hasDelta(String relpath);

  /**
   * Returns <code>true</code> if any file or folder identified by <code>relpaths</code> has 
   * changed since last build.
   *  
   * @param relpaths List<String> are paths relative to build context basedir
   */
  boolean hasDelta(List relpaths);

  /**
   * Indicates that the file content has been modified during the build.
   * 
   * @see #newFileOutputStream(File)
   */
  void refresh(File file);

  /**
   * Returns new OutputStream that writes to the <code>file</code>.
   *  
   * Files changed using OutputStream returned by this method do not need to be
   * explicitly refreshed using {@link #refresh(File)}.
   */
  OutputStream newFileOutputStream(File file) throws IOException;

  /**
   * Convenience method, fully equal to newScanner(basedir, false)
   */
  Scanner newScanner(File basedir);

  /**
   * Returned Scanner scans <code>basedir</code> for files and directories 
   * deleted since last build. Returns empty Scanner if <code>basedir</code>
   * is not under this build context basedir.
   */
  Scanner newDeleteScanner(File basedir);

  /**
   * Returned Scanner scans files and folders under <code>basedir</code>.
   * If <code>ignoreDelta</code> is <code>false</code>, the scanner will only
   * "see" files and folders with content changes since last build. If 
   * <code>ignoreDelta</code> is <code>true</code>, the scanner will "see" all
   * files and folders. Returns empty Scanner if <code>basedir</code>
   * is not under this build context basedir.
   */
  Scanner newScanner(File basedir, boolean ignoreDelta);
}
