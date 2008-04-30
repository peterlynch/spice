/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.index;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.sonatype.nexus.index.context.IndexingContext;

public class IndexUtils
{

    public static final String TIMESTAMP_FILE = "timestamp";

    // timestamp

    public static void updateTimestamp( Directory directory, Date timestamp )
        throws IOException
    {
        synchronized ( directory )
        {
            if ( directory.fileExists( TIMESTAMP_FILE ) )
            {
                directory.deleteFile( TIMESTAMP_FILE );
            }

            IndexOutput io = directory.createOutput( TIMESTAMP_FILE );

            try
            {
                io.writeLong( timestamp.getTime() );
                
                io.flush();
            }
            finally
            {
                close( io );
            }
        }
    }

    public static Date getTimestamp( Directory directory )
    {
        synchronized ( directory )
        {
            Date result = null;
            try
            {
                if ( directory.fileExists( TIMESTAMP_FILE ) )
                {
                    IndexInput ii = null;
                    
                    try
                    {
                        ii = directory.openInput( TIMESTAMP_FILE );

                        result = new Date( ii.readLong() );

                        ii.close();
                    }
                    finally
                    {
                        if ( ii != null )
                        {
                            ii.close();
                        }
                    }
                }
            }
            catch ( IOException ex )
            {
            }

            return result;
        }
    }

    // pack/unpack

    public static Date getIndexArchiveTime( InputStream is )
        throws IOException
    {
        ZipInputStream zis = null;
        try
        {
            zis = new ZipInputStream( is );

            long timestamp = -1;

            ZipEntry entry;
            while ( ( entry = zis.getNextEntry() ) != null )
            {
                if ( entry.getName() == IndexUtils.TIMESTAMP_FILE )
                {
                    return new Date( new DataInputStream( zis ).readLong() );
                }
                timestamp = entry.getTime();
            }

            return timestamp == -1 ? null : new Date( timestamp );
        }
        finally
        {
            close( zis );
            close( is );
        }
    }

    /**
     * Unpacks index data into specified Lucene <code>Directory</code>
     * 
     * @param is a <code>ZipInputStream</code> with index data
     * @param directory Lucene <code>Directory</code> to unpack index data to
     * @return {@link Date} of the index update or null if it can't be read
     */
    public static Date unpackIndexArchive( InputStream is, Directory directory )
        throws IOException
    {

        ZipInputStream zis = new ZipInputStream( is );
        try
        {
            byte[] buf = new byte[4096];

            ZipEntry entry;

            while ( ( entry = zis.getNextEntry() ) != null )
            {
                if ( entry.isDirectory() || entry.getName().indexOf( '/' ) > -1 )
                {
                    continue;
                }

                IndexOutput io = directory.createOutput( entry.getName() );

                try
                {
                    int n = 0;

                    while ( ( n = zis.read( buf ) ) != -1 )
                    {
                        io.writeBytes( buf, n );
                    }
                }
                finally
                {
                    close( io );
                }
            }
        }
        finally
        {
            close( zis );
        }

        return IndexUtils.getTimestamp( directory );
    }

    public static void packIndexArchive( IndexingContext context, OutputStream os )
        throws IOException
    {
        ZipOutputStream zos = null;

        // force the timestamp update
        updateTimestamp( context.getIndexDirectory(), context.getTimestamp() );

        try
        {
            zos = new ZipOutputStream( os );

            zos.setLevel( 9 );

            String[] names = context.getIndexDirectory().list();

            boolean savedTimestamp = false;

            for ( int i = 0; i < names.length; i++ )
            {
                String name = names[i];

                writeFile( name, zos, context.getIndexDirectory() );

                if ( name.equals( TIMESTAMP_FILE ) )
                {
                    savedTimestamp = true;
                }
            }

            // FSDirectory filter out the foreign files
            if ( !savedTimestamp && context.getIndexDirectory().fileExists( TIMESTAMP_FILE ) )
            {
                writeFile( TIMESTAMP_FILE, zos, context.getIndexDirectory() );
            }
        }
        finally
        {
            close( zos );
        }
    }

    private static void writeFile( String name, ZipOutputStream zos, Directory directory )
        throws IOException
    {
        ZipEntry e = new ZipEntry( name );

        zos.putNextEntry( e );

        IndexInput in = directory.openInput( name );

        try
        {
            int len = (int) in.length();

            byte[] buf = new byte[len];

            in.readBytes( buf, 0, len );

            zos.write( buf, 0, len );

        }
        finally
        {
            close( in );
        }

        zos.flush();

        zos.closeEntry();
    }

    //

    private static void close( OutputStream os )
    {
        if ( os != null )
        {
            try
            {
                os.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    private static void close( InputStream is )
    {
        if ( is != null )
        {
            try
            {
                is.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    private static void close( IndexOutput io )
    {
        if ( io != null )
        {
            try
            {
                io.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    private static void close( IndexInput in )
    {
        if ( in != null )
        {
            try
            {
                in.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

}
