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
package org.sonatype.plexus.webcontainer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Jason van Zyl
 * @plexus.component
 */
public class DefaultArchiver
    implements Archiver
{
    public void zip( File sourceDirectory, File archive )
        throws IOException
    {
        String name = sourceDirectory.getName();

        ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( new File( sourceDirectory.getParent(), name
            + ".zip" ) ) );

        try
        {
            
            zos.setLevel( 9 );
    
            File[] files = sourceDirectory.listFiles();
    
            for ( int i = 0; i < files.length; i++ )
            {
                ZipEntry e = new ZipEntry( files[i].getName() );
    
                zos.putNextEntry( e );
    
                FileInputStream is = new FileInputStream( files[i] );
    
                try
                {
                    byte[] buf = new byte[4096];
        
                    int n;
        
                    while ( ( n = is.read( buf ) ) > 0 )
                    {
                        zos.write( buf, 0, n );
                    }
                }
                finally
                {
                    is.close();
                }
        
                zos.flush();
    
                zos.closeEntry();
            }
        }
        finally
        {
            zos.close();
        }
    }

    public void unzip( File archive, File targetDirectory )
        throws IOException
    {
        InputStream in = new BufferedInputStream( new FileInputStream( archive ) );

        ZipInputStream zin = new ZipInputStream( in );
        
        try
        {        
            ZipEntry e;
        
            while ( ( e = zin.getNextEntry() ) != null )
            {
                File f = new File( targetDirectory, e.getName() );

                // We don't want to want to write out directories, just files.
                if ( e.isDirectory() )
                {
                    continue;
                }
                
                // Make the directory for the target file if it doesn't exist.
                if ( !f.getParentFile().exists() )
                {
                    f.getParentFile().mkdirs();
                }
                
                FileOutputStream out = new FileOutputStream( f );
        
                try
                {
                    byte[] b = new byte[512];
            
                    int len;
            
                    while ( ( len = zin.read( b ) ) != -1 )
                    {
                        out.write( b, 0, len );
                    }
                }
                finally
                {
                  out.close();
                }
            }
        }
        finally
        {
            zin.close();
        }
    }
}
