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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

public class TimelinePersistorTest
    extends AbstractTimelineTestCase
{
    protected File persistDirectory;

    protected TimelineConfiguration configuration;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        persistDirectory = new File( PlexusTestCase.getBasedir(), "target/persist" );

        cleanDirectory( persistDirectory );

        this.configuration = new TimelineConfiguration( persistDirectory, null );

        persistor.configure( configuration );
    }

    public void testPersistSingleRecord()
        throws Exception
    {
        TimelineRecord record = createTimelineRecord();

        persistor.persist( record );

        List<TimelineRecord> results = toList( persistor.readAll() );

        assertEquals( 1, results.size() );
        assertEquals( record.getTimestamp(), results.get( 0 ).getTimestamp() );
        assertEquals( record.getType(), results.get( 0 ).getType() );
        assertEquals( record.getSubType(), results.get( 0 ).getSubType() );
        assertEquals( record.getData(), results.get( 0 ).getData() );
    }

    public void testPersistMultipleRecords()
        throws Exception
    {
        long timestamp1 = System.currentTimeMillis();
        String type1 = "type";
        String subType1 = "subType";
        Map<String, String> data1 = new HashMap<String, String>();
        data1.put( "k1", "v1" );
        data1.put( "k2", "v2" );

        TimelineRecord record1 = createTimelineRecord( timestamp1, type1, subType1, data1 );

        long timestamp2 = System.currentTimeMillis();
        String type2 = "type2";
        String subType2 = "subType2";
        Map<String, String> data2 = new HashMap<String, String>();
        data2.put( "k21", "v21" );
        data2.put( "k22", "v22" );

        TimelineRecord record2 = createTimelineRecord( timestamp2, type2, subType2, data2 );

        persistor.persist( record1 );
        persistor.persist( record2 );

        List<TimelineRecord> results = toList( persistor.readAll() );

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

        assertEquals( count, toList( persistor.readAll() ).size() );
    }

    public void testRolling()
        throws Exception
    {
        persistor.configure( new TimelineConfiguration( persistDirectory, persistDirectory, 1 ) );

        persistor.persist( createTimelineRecord() );
        persistor.persist( createTimelineRecord() );

        Thread.sleep( 1100 );

        persistor.persist( createTimelineRecord() );

        assertEquals( 2, persistDirectory.listFiles().length );

        assertEquals( 3, toList( persistor.readAll() ).size() );
    }

    public void testIllegalDataFile()
        throws Exception
    {
        persistor.persist( createTimelineRecord() );

        File badFile = new File( persistDirectory, "bad.txt" );

        FileUtils.fileWrite( badFile.getAbsolutePath(), "some bad data" );

        assertEquals( 1, toList( persistor.readAll() ).size() );
    }

    public void testDataKeyNull()
        throws Exception
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( null, "v1" );
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), "type", "subType", data );

        try
        {
            persistor.persist( record );

            fail( "key is null, should throw TimelineException." );
        }
        catch ( TimelineException e )
        {
            // expected
        }
    }

    public void testDataValueNull()
        throws Exception
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", null );
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), "type", "subType", data );

        try
        {
            persistor.persist( record );

            fail( "value is null, should throw TimelineException." );
        }
        catch ( TimelineException e )
        {
            // expected
        }
    }

    public void testDataKeyEmpty()
        throws Exception
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( "", "v1" );
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), "type", "subType", data );

        persistor.persist( record );
    }

    public void testDataValueEmpty()
        throws Exception
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "" );
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), "type", "subType", data );

        persistor.persist( record );
    }

    public void testSubTypeNull()
        throws Exception
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), "type", null, data );

        persistor.persist( record );
    }

    public void testTypeNull()
        throws Exception
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), null, "subType", data );

        persistor.persist( record );
    }

    public void testDataNull()
        throws Exception
    {
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), "type", "subType", null );

        persistor.persist( record );
    }

    // ==

    /**
     * This gathers all elements into a list. Usable for testing only, since this will try to keep all records in
     * memory! Use this only in UTs!!! You've been warned.
     * 
     * @param iterable
     * @return
     */
    protected List<TimelineRecord> toList( Iterable<TimelineRecord> iterable )
    {
        ArrayList<TimelineRecord> result = new ArrayList<TimelineRecord>();

        for ( TimelineRecord rec : iterable )
        {
            result.add( rec );
        }

        return result;
    }
}
