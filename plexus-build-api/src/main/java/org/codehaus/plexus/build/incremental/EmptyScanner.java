
package org.codehaus.plexus.build.incremental;

import org.codehaus.plexus.util.Scanner;

/**
 * Scanner implementation never finds any files/directories.
 */
public class EmptyScanner implements Scanner {
  
  public static final Scanner INSTANCE = new EmptyScanner();
  
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

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

}
