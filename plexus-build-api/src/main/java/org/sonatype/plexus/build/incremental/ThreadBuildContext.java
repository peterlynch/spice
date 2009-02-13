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

import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;
import org.codehaus.plexus.util.Scanner;

/**
 * BuildContext implementation that delegates actual work to thread-local
 * build context set using {@link #setThreadBuildContext(BuildContext)}.
 * {@link DefaultBuildContext} is used if no thread local build context was set.
 * 
 * Note that plexus component metadata is not generated for this implementation.
 * Apparently, older version of plexus used by maven-filtering and likely
 * other projects, does not honour "default" role-hint.
 */
public class ThreadBuildContext implements BuildContext {

  private static final ThreadLocal threadContext = new ThreadLocal();

  private static final DefaultBuildContext defaultContext = new DefaultBuildContext();

  public static BuildContext getContext() {
    BuildContext context = (BuildContext) threadContext.get();
    if(context == null) {
      context = defaultContext;
    }
    return context;
  }

  public static void setThreadBuildContext(BuildContext context) {
    threadContext.set(context);
  }

  public boolean hasDelta(String relPath) {
    return getContext().hasDelta(relPath);
  }

  public boolean hasDelta(List relPaths) {
    return getContext().hasDelta(relPaths);
  }

  public Scanner newDeleteScanner(File basedir) {
    return getContext().newDeleteScanner(basedir);
  }

  public OutputStream newFileOutputStream(File file) throws IOException {
    return getContext().newFileOutputStream(file);
  }

  public Scanner newScanner(File basedir) {
    return getContext().newScanner(basedir);
  }

  public Scanner newScanner(File basedir, boolean ignoreDelta) {
    return getContext().newScanner(basedir, ignoreDelta);
  }

  public void refresh(File file) {
    getContext().refresh(file);
  }

  public Object getValue(String key) {
    return getContext().getValue(key);
  }

  public boolean isIncremental() {
    return getContext().isIncremental();
  }

  public void setValue(String key, Object value) {
    getContext().setValue(key, value);
  }

}
