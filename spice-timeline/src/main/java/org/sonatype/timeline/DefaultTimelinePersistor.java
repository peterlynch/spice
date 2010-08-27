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
package org.sonatype.timeline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.timeline.proto.TimeLineRecordProtos;

/**
 * @author juven
 */
@Component( role = TimelinePersistor.class )
public class DefaultTimelinePersistor
    implements TimelinePersistor
{
    public static final String DATA_FILE_NAME_PATTERN =
        "^timeline\\.\\d{4}-\\d{2}-\\d{2}\\.\\d{2}-\\d{2}-\\d{2}\\.dat$";

    @Requirement
    private Logger logger;

    private int rollingInterval;

    private File persistDirectory;

    private long lastRolledTimestamp = 0L;

    private File lastRolledFile;
    
    protected Logger getLogger()
    {
        return logger;
    }

    public void configure( TimelineConfiguration config )
    {
        this.persistDirectory = config.getPersistDirectory();

        if ( !this.persistDirectory.exists() )
        {
            this.persistDirectory.mkdirs();
        }

        this.rollingInterval = config.getPersistRollingInterval();
    }

    public void persist( TimelineRecord record )
        throws TimelineException
    {
        verify( record );

        OutputStream out = null;

        synchronized ( this )
        {
            try
            {
                out = new FileOutputStream( getDataFile(), true );

                byte[] bytes = toProto( record ).toByteArray();

                out.write( bytes.length );

                out.write( bytes );

            }
            catch ( IOException e )
            {
                throw new TimelineException( "Failed to persist timeline record to data file!", e );
            }
            finally
            {
                if ( out != null )
                {
                    try
                    {
                        out.close();
                    }
                    catch ( IOException e )
                    {
                    }
                }
            }
        }
    }

    public TimelineResult readAll()
        throws TimelineException
    {
        return new PersistorTimelineResult( persistDirectory.listFiles() );
    }

    private static class PersistorTimelineResult
        extends TimelineResult
    {
        private final File[] fromFiles;

        private int filePtr;

        private Iterator<TimelineRecord> currentIterator;

        public PersistorTimelineResult( File[] files )
        {
            this.fromFiles = files;

            this.filePtr = 0;

            this.currentIterator = null;
        }

        @Override
        protected TimelineRecord fetchNextRecord()
        {
            if ( currentIterator != null && currentIterator.hasNext() )
            {
                return currentIterator.next();
            }
            else if ( filePtr >= fromFiles.length )
            {
                // no more
                return null;
            }
            else
            {
                try
                {
                    currentIterator = readFile( fromFiles[filePtr] );

                    filePtr++;
                }
                catch ( TimelineException e )
                {
                    // skip it?
                    // throw new IllegalStateException( "Cannot fetch next iterator from file: " + fromFiles[filePtr], e );
                    filePtr++;
                }

                return fetchNextRecord();
            }
        }

        private Iterator<TimelineRecord> readFile( File file )
            throws TimelineException
        {
            List<TimelineRecord> result = new ArrayList<TimelineRecord>();

            synchronized ( this )
            {
                InputStream in = null;

                try
                {
                    in = new FileInputStream( file );

                    while ( in.available() > 0 )
                    {
                        int length = in.read();

                        byte[] bytes = new byte[length];

                        in.read( bytes, 0, length );

                        result.add( fromProto( TimeLineRecordProtos.TimeLineRecord.parseFrom( bytes ) ) );
                    }

                }
                catch ( Exception e )
                {
                    throw new TimelineException( "Failed to read timeline record from data file!", e );
                }
                finally
                {
                    if ( in != null )
                    {
                        try
                        {
                            in.close();
                        }
                        catch ( IOException e )
                        {
                        }
                    }
                }
            }

            return result.iterator();
        }

        private TimelineRecord fromProto( TimeLineRecordProtos.TimeLineRecord rec )
        {
            Map<String, String> dataMap = new HashMap<String, String>();

            for ( TimeLineRecordProtos.TimeLineRecord.Data data : rec.getDataList() )
            {
                dataMap.put( data.getKey(), data.getValue() );
            }

            return new TimelineRecord( rec.getTimestamp(), rec.getType(), rec.getSubType(), dataMap );
        }
        
        @Override
        protected void doRelease()
            throws IOException
        {
        }

    }

    // ==

    private File getDataFile()
        throws IOException
    {
        long now = System.currentTimeMillis();

        if ( lastRolledTimestamp == 0L || ( now - lastRolledTimestamp ) > ( rollingInterval * 1000 ) )
        {
            lastRolledTimestamp = now;

            lastRolledFile = new File( persistDirectory, buildTimestampedFileName() );

            lastRolledFile.createNewFile();
        }

        return lastRolledFile;
    }

    private String buildTimestampedFileName()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd.HH-mm-ss" );

        StringBuffer fileName = new StringBuffer();

        fileName.append( "timeline." ).append( dateFormat.format( new Date() ) ).append( ".dat" );

        return fileName.toString();
    }

    private TimeLineRecordProtos.TimeLineRecord toProto( TimelineRecord record )
    {
        TimeLineRecordProtos.TimeLineRecord.Builder builder = TimeLineRecordProtos.TimeLineRecord.newBuilder();

        builder.setTimestamp( record.getTimestamp() );

        builder.setType( record.getType() );

        builder.setSubType( record.getSubType() );

        for ( Map.Entry<String, String> entry : record.getData().entrySet() )
        {
            builder.addData( TimeLineRecordProtos.TimeLineRecord.Data.newBuilder().setKey( entry.getKey() ).setValue(
                entry.getValue() ).build() );
        }

        return builder.build();
    }

    private void verify( TimelineRecord record )
        throws TimelineException
    {
        Map<String, String> data = record.getData();

        if ( data == null )
        {
            return;
        }

        for ( Map.Entry<String, String> entry : data.entrySet() )
        {
            if ( entry.getKey() == null )
            {
                throw new TimelineException( "Timeline record contains invalid data: key is null." );
            }
            if ( entry.getValue() == null )
            {
                throw new TimelineException( "Timeline record contains invalid data: value is null." );
            }
        }
    }
}
