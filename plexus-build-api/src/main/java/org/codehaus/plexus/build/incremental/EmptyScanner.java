
package org.codehaus.plexus.build.incremental;

import java.io.File;

import org.codehaus.plexus.util.Scanner;

/**
 * Scanner implementation never finds any files/directories.
 */
public class EmptyScanner implements Scanner {
  
  private static final String[] EMPTY_STRING_ARRAY = new String[0];
  
  private final File basedir;
  
  public EmptyScanner(File basedir) {
    this.basedir = basedir;
  }

  public void addDefaultExcludes() {
  }

  public String[] getIncludedDirectories() {
    return EMPTY_STRING_ARRAY;
  }

  public String[] getIncludedFiles() {
    return EMPTY_STRING_ARRAY;
  }

  public void scan() {
  }

  public void setExcludes(String[] excludes) {
  }

  public void setIncludes(String[] includes) {
  }

  public File getBasedir() {
    return basedir;
  }

}
