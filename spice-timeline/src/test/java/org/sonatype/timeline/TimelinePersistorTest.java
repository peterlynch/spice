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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

public class TimelinePersistorTest
    extends PlexusTestCase
{
    protected TimelinePersistor persistor;

    protected File persistDirectory;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        persistor = this.lookup( TimelinePersistor.class );

        persistDirectory = new File( PlexusTestCase.getBasedir(), "target/timeline" );

        if ( persistDirectory.exists() )
        {
            for ( File file : persistDirectory.listFiles() )
            {
                file.delete();
            }

            persistDirectory.delete();
        }

        persistor.configure( new File( PlexusTestCase.getBasedir(), "target/timeline" ) );
    }

    public void testPersistSingleRecord()
        throws Exception
    {
        long timestamp = new Date().getTime();
        String type = "typte";
        String subType = "subType";
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1 test", "v1" );
        data.put( "k2", "v2" );

        TimelineRecord record = new TimelineRecord();
        record.setTimestamp( timestamp );
        record.setType( type );
        record.setSubType( subType );
        record.setData( data );

        persistor.persist( record );

        List<TimelineRecord> results = persistor.readAll();

        assertEquals( 1, results.size() );
        assertEquals( timestamp, results.get( 0 ).getTimestamp() );
        assertEquals( type, results.get( 0 ).getType() );
        assertEquals( subType, results.get( 0 ).getSubType() );
        assertEquals( data, results.get( 0 ).getData() );
    }

    public void testPersistMultipleRecords()
        throws Exception
    {
        long timestamp1 = new Date().getTime();
        String type1 = "type";
        String subType1 = "subType";
        Map<String, String> data1 = new HashMap<String, String>();
        data1.put( "k1", "v1" );
        data1.put( "k2", "v2" );

        TimelineRecord record1 = new TimelineRecord();
        record1.setTimestamp( timestamp1 );
        record1.setType( type1 );
        record1.setSubType( subType1 );
        record1.setData( data1 );

        long timestamp2 = new Date().getTime();
        String type2 = "type2";
        String subType2 = "subType2";
        Map<String, String> data2 = new HashMap<String, String>();
        data2.put( "k21", "v21" );
        data2.put( "k22", "v22" );

        TimelineRecord record2 = new TimelineRecord();
        record2.setTimestamp( timestamp2 );
        record2.setType( type2 );
        record2.setSubType( subType2 );
        record2.setData( data2 );

        persistor.persist( record1 );
        persistor.persist( record2 );

        List<TimelineRecord> results = persistor.readAll();

        assertEquals( 2, results.size() );

        assertEquals( timestamp1, results.get( 0 ).getTimestamp() );
        assertEquals( type1, results.get( 0 ).getType() );
        assertEquals( subType1, results.get( 0 ).getSubType() );
        assertEquals( data1, results.get( 0 ).getData() );

        assertEquals( timestamp2, results.get( 1 ).getTimestamp() );
        assertEquals( type2, results.get( 1 ).getType() );
        assertEquals( subType2, results.get( 1 ).getSubType() );
        assertEquals( data2, results.get( 1 ).getData() );
    }

    public void testPersistLotsOfRecords()
        throws Exception
    {
        final int count = 500;

        for ( int i = 0; i < count; i++ )
        {
            persistor.persist( createTimelineRecord() );
        }

        assertEquals( count, persistor.readAll().size() );
    }

    public void testRolling()
        throws Exception
    {
        persistor.configure( new File( PlexusTestCase.getBasedir(), "target/timeline" ), 1 );

        persistor.persist( createTimelineRecord() );
        persistor.persist( createTimelineRecord() );

        Thread.sleep( 1100 );

        persistor.persist( createTimelineRecord() );

        assertEquals( 2, persistDirectory.listFiles().length );

        assertEquals( 3, persistor.readAll().size() );
    }

    public void testIllegalDataFile()
        throws Exception
    {
        persistor.configure( new File( PlexusTestCase.getBasedir(), "target/timeline" ) );

        persistor.persist( createTimelineRecord() );

        File badFile = new File( PlexusTestCase.getBasedir(), "target/timeline/bad.txt" );

        FileUtils.fileWrite( badFile.getAbsolutePath(), "some bad data" );

        assertEquals( 1, persistor.readAll().size() );
    }

    private TimelineRecord createTimelineRecord()
    {
        TimelineRecord record = new TimelineRecord();
        record.setTimestamp( new Date().getTime() );
        record.setType( "type" );
        record.setSubType( "subType" );
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
        data.put( "k2", "v2" );
        data.put( "k3", "v3" );
        data.put( "k4", "v4" );
        record.setData( data );

        return record;
    }
}
