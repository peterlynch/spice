/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.appbooter;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.sonatype.appbooter.ctl.AppBooterServiceException;
import org.sonatype.plexus.classworlds.model.ClassworldsRealmConfiguration;

/**
 * The default implementation of ForkedAppBooter, a.k.a. a Simple Plexus Component. If a '*' is found in the file name
 * (NOT in the directory names), this implementation will try to resolve the file first. For example if the
 * <code>platformFile</code> is '/mypath/directory/myJar-*.jar', this may resolve to
 * '/mypath/directory/myJar-everChangingVersion.jar'. <BR/>
 * <BR/>
 * The regex pattern for this would be '/mypath/directory/myJar-.*\.jar'.
 * 
 * @see AbstractForkedAppBooter
 * @plexus.component instantiation-strategy="per-lookup" role="org.sonatype.appbooter.ForkedAppBooter"
 *                   role-hint="DefaultForkedAppBooter"
 */
public class DefaultForkedAppBooter
    extends AbstractForkedAppBooter
{
    /**
     * @plexus.configuration default-value="some-jar"
     */
    private String platformFile;

    /**
     * @plexus.configuration default-value="{${basedir}/runtime/apps/nexus/conf,
     *                       ${basedir}/runtime/apps/nexus/lib/*.jar}"
     */
    private List<String> classPathElements;

    public ClassworldsRealmConfiguration getClassworldsRealmConfig()
    {
        ClassworldsRealmConfiguration rootRealmConfig = new ClassworldsRealmConfiguration( "plexus" );

        rootRealmConfig.addLoadPatterns( classPathElements );
        return rootRealmConfig;
    }

    public File getPlatformFile()
        throws AppBooterServiceException
    {
        return resolvePlatformFile( new File( platformFile ) );
    }

    public void setPlatformFile( File f )
    {
        this.platformFile = f.getAbsolutePath();
    }

    /**
     * This is slightly more then a default case, but we will check if the file with the exact name exists first. That
     * should make everyone happy.
     * 
     * @param platformFile
     * @return
     */
    private File resolvePlatformFile( File platformFile )
    {

        // firt check if the file exists && and has a parent
        if ( !platformFile.exists() && platformFile.getParentFile() != null )
        {

            // now do the fun stuff. A little file finding regex fun

            // we want to excape all of the current . and replace them with \., and replace any * with .*
            final String fileRegEx =
                platformFile.getAbsolutePath().replace( "\\", "\\\\" ).replace( ".", "\\." ).replace( "*", ".*" );
            // This may be a little to non standard. but it would be really easy to add a new component to replace the
            // default.

            File parent = platformFile.getParentFile();

            // TODO replace this with a standard filter
            File[] files = parent.listFiles( new FileFilter()
            {

                public boolean accept( File pathname )
                {

                    return pathname.getAbsolutePath().matches( fileRegEx );

                }
            } );

            // if we have more then one match that is too bad, use the first one...

            // we can let them know there was more then one file
            if ( files != null && files.length > 1 ) // yeah, i know the null check is a little much...
            {
                this.getLogger().warn( "More then one file matched: " + platformFile + " using: " + files[0] );
            }

            // now if we have any files at all use the first and assign it.
            if ( files != null && files.length > 0 )
            {
                this.getLogger().debug( "Platform File: " + platformFile + " resolved to: " + files[0] );
                platformFile = files[0];
            }
        }
        return platformFile;
    }

}
