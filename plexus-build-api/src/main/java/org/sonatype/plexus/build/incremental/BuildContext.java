/*
Copyright (c) 2008 Sonatype, Inc. All rights reserved.

This program is licensed to you under the Apache License Version 2.0, 
and you may not use this file except in compliance with the Apache License Version 2.0. 
You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, 
software distributed under the Apache License Version 2.0 is distributed on an 
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
*/
package org.sonatype.plexus.build.incremental;

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
   * Indicates that the file or folder content has been modified during the build.
   * 
   * @see #newFileOutputStream(File)
   */
  void refresh(File file);

  /**
   * Returns new OutputStream that writes to the <code>file</code>.
   *  
   * Files changed using OutputStream returned by this method do not need to be
   * explicitly refreshed using {@link #refresh(File)}.
   *
   * As an optional optimisation, OutputStreams created by incremental build 
   * context will attempt to avoid writing to the file if file content 
   * has not changed.
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
   * 
   * If this is an incremental build context and  <code>ignoreDelta</code> 
   * is <code>false</code>, the scanner will only "see" files and folders with 
   * content changes since last build. 
   * 
   * If <code>ignoreDelta</code> is <code>true</code>, the scanner will "see" all
   * files and folders. 
   * 
   * Returns empty Scanner if <code>basedir</code> is not under this build context basedir.
   */
  Scanner newScanner(File basedir, boolean ignoreDelta);

  /**
   * Returns <code>true</code> if this build context is incremental. 
   * 
   * Scanners created by {@link #newScanner(File)} of an incremental build context
   * will ignore files and folders that were not changed since last build. 
   * Additionally, {@link #newDeleteScanner(File)} will scan files and directories
   * deleted since last build.
   */
  boolean isIncremental();

  /**
   * Associate specified <code>key</code> with specified <code>value</code>
   * in the build context.
   * 
   * Primary (and the only) purpose of this method is to allow preservation of 
   * state needed for proper incremental behaviour between consecutive executions 
   * of the same mojo needed to. 
   * 
   * For example, maven-plugin-plugin:descriptor mojo
   * can store collection of extracted MojoDescritpor during first invocation. Then
   * on each consecutive execution maven-plugin-plugin:descriptor will only need
   * to extract MojoDescriptors for changed files.
   *
   * @see #getValue(String)
   */
  public void setValue(String key, Object value);

  /**
   * Returns value associated with <code>key</code> during previous mojo execution.
   * 
   * This method always returns <code>null</code> for non-incremental builds
   * (i.e., {@link #isIncremental()} returns <code>false</code>) and mojos are 
   * expected to fall back to full, non-incremental behaviour.
   * 
   * @see #setValue(String, Object)
   * @see #isIncremental()
   */
  public Object getValue(String key);

}
