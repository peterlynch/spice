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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusTestCase;

public class TimelineIndexerTest
    extends PlexusTestCase
{
    protected TimelineIndexer indexer;

    protected File indexDirectory;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        indexer = this.lookup( TimelineIndexer.class );

        indexDirectory = new File( PlexusTestCase.getBasedir(), "target/timeline" );

        if ( indexDirectory.exists() )
        {
            for ( File file : indexDirectory.listFiles() )
            {
                file.delete();
            }
            indexDirectory.delete();
        }

        indexer.configure( indexDirectory );
    }

    public void testIndexOneRecord()
        throws Exception
    {
        TimelineRecord record = createTimelineRecord();
        indexer.add( record );

        Set<String> types = new HashSet<String>();
        types.add( "type" );
        List<Map<String, String>> results = indexer.retrieve( 0, System.currentTimeMillis(), types, null, 0, 100, null );

        assertEquals( 1, results.size() );
        assertEquals( record.getData(), results.get( 0 ) );
    }

    public void testIndexMutipleRecords()
        throws Exception
    {
        for ( int i = 0; i < 10; i++ )
        {
            indexer.add( createTimelineRecord() );
        }

        List<Map<String, String>> results = indexer.retrieve( 0, System.currentTimeMillis(), null, null, 0, 100, null );

        assertEquals( 10, results.size() );
    }

    public void testSearchByTime()
        throws Exception
    {
        TimelineRecord rec1 = createTimelineRecord();
        rec1.setTimestamp( 1000000L );
        TimelineRecord rec2 = createTimelineRecord();
        rec2.setTimestamp( 2000000L );
        TimelineRecord rec3 = createTimelineRecord();
        rec3.setTimestamp( 3000000L );
        TimelineRecord rec4 = createTimelineRecord();
        rec4.setTimestamp( 4000000L );
        indexer.add( rec1 );
        indexer.add( rec2 );
        indexer.add( rec3 );
        indexer.add( rec4 );

        assertEquals( 4, indexer.retrieve( 0, System.currentTimeMillis(), null, null, 0, 100, null ).size() );
        assertEquals( 3, indexer.retrieve( 0, 3500000L, null, null, 0, 100, null ).size() );
        assertEquals( 2, indexer.retrieve( 1500000L, 3500000L, null, null, 0, 100, null ).size() );
        assertEquals( 0, indexer.retrieve( 4500000L, System.currentTimeMillis(), null, null, 0, 100, null ).size() );
    }

    public void testSearchByType()
        throws Exception
    {
        TimelineRecord rec1 = createTimelineRecord();
        rec1.setType( "typeA" );
        TimelineRecord rec2 = createTimelineRecord();
        rec2.setType( "typeB" );
        TimelineRecord rec3 = createTimelineRecord();
        rec3.setType( "typeB" );
        TimelineRecord rec4 = createTimelineRecord();
        rec4.setType( "typeC" );
        indexer.add( rec1 );
        indexer.add( rec2 );
        indexer.add( rec3 );
        indexer.add( rec4 );

        Set<String> types = new HashSet<String>();

        types.add( "typeA" );
        assertEquals( 1, indexer.retrieve( 0, System.currentTimeMillis(), types, null, 0, 100, null ).size() );
        types.clear();
        types.add( "typeB" );
        assertEquals( 2, indexer.retrieve( 0, System.currentTimeMillis(), types, null, 0, 100, null ).size() );
        types.clear();
        types.add( "typeA" );
        types.add( "typeB" );
        assertEquals( 3, indexer.retrieve( 0, System.currentTimeMillis(), types, null, 0, 100, null ).size() );
        types.clear();
        types.add( "typeA" );
        types.add( "typeB" );
        types.add( "typeC" );
        assertEquals( 4, indexer.retrieve( 0, System.currentTimeMillis(), types, null, 0, 100, null ).size() );
        types.clear();
        assertEquals( 4, indexer.retrieve( 0, System.currentTimeMillis(), types, null, 0, 100, null ).size() );
        types.clear();
        types.add( "typeX" );
        assertEquals( 0, indexer.retrieve( 0, System.currentTimeMillis(), types, null, 0, 100, null ).size() );
    }

    public void testSearchBySubType()
        throws Exception
    {
        TimelineRecord rec1 = createTimelineRecord();
        rec1.setSubType( "subA" );
        TimelineRecord rec2 = createTimelineRecord();
        rec2.setSubType( "subB" );
        TimelineRecord rec3 = createTimelineRecord();
        rec3.setSubType( "subB" );
        TimelineRecord rec4 = createTimelineRecord();
        rec4.setSubType( "subC" );
        indexer.add( rec1 );
        indexer.add( rec2 );
        indexer.add( rec3 );
        indexer.add( rec4 );

        Set<String> subTypes = new HashSet<String>();

        subTypes.add( "subA" );
        assertEquals( 1, indexer.retrieve( 0, System.currentTimeMillis(), null, subTypes, 0, 100, null ).size() );
        subTypes.clear();
        subTypes.add( "subB" );
        assertEquals( 2, indexer.retrieve( 0, System.currentTimeMillis(), null, subTypes, 0, 100, null ).size() );
        subTypes.clear();
        subTypes.add( "subA" );
        subTypes.add( "subB" );
        assertEquals( 3, indexer.retrieve( 0, System.currentTimeMillis(), null, subTypes, 0, 100, null ).size() );
        subTypes.clear();
        subTypes.add( "subA" );
        subTypes.add( "subB" );
        subTypes.add( "subC" );
        assertEquals( 4, indexer.retrieve( 0, System.currentTimeMillis(), null, subTypes, 0, 100, null ).size() );
        subTypes.clear();
        assertEquals( 4, indexer.retrieve( 0, System.currentTimeMillis(), null, subTypes, 0, 100, null ).size() );
        subTypes.clear();
        subTypes.add( "subX" );
        assertEquals( 0, indexer.retrieve( 0, System.currentTimeMillis(), null, subTypes, 0, 100, null ).size() );
    }

    public void testSearchByTypeAndSubType()
        throws Exception
    {
        TimelineRecord rec1 = createTimelineRecord();
        rec1.setType( "typeA" );
        rec1.setSubType( "subX" );
        TimelineRecord rec2 = createTimelineRecord();
        rec2.setType( "typeB" );
        rec2.setSubType( "subX" );
        TimelineRecord rec3 = createTimelineRecord();
        rec3.setType( "typeB" );
        rec3.setSubType( "subY" );
        TimelineRecord rec4 = createTimelineRecord();
        rec4.setType( "typeA" );
        rec4.setSubType( "subX" );
        indexer.add( rec1 );
        indexer.add( rec2 );
        indexer.add( rec3 );
        indexer.add( rec4 );

        Set<String> types = new HashSet<String>();
        Set<String> subTypes = new HashSet<String>();

        types.add( "typeA" );
        subTypes.add( "subX" );
        assertEquals( 2, indexer.retrieve( 0, System.currentTimeMillis(), types, subTypes, 0, 100, null ).size() );

        types.clear();
        subTypes.clear();
        types.add( "typeB" );
        subTypes.add( "subX" );
        assertEquals( 1, indexer.retrieve( 0, System.currentTimeMillis(), types, subTypes, 0, 100, null ).size() );

        types.clear();
        subTypes.clear();
        types.add( "typeA" );
        types.add( "typeB" );
        subTypes.add( "subX" );
        assertEquals( 3, indexer.retrieve( 0, System.currentTimeMillis(), types, subTypes, 0, 100, null ).size() );

        types.clear();
        subTypes.clear();
        types.add( "typeA" );
        subTypes.add( "subY" );
        assertEquals( 0, indexer.retrieve( 0, System.currentTimeMillis(), types, subTypes, 0, 100, null ).size() );

        types.clear();
        subTypes.clear();
        types.add( "typeB" );
        subTypes.add( "subY" );
        assertEquals( 1, indexer.retrieve( 0, System.currentTimeMillis(), types, subTypes, 0, 100, null ).size() );

        types.clear();
        subTypes.clear();
        types.add( "typeA" );
        types.add( "typeB" );
        subTypes.add( "subX" );
        subTypes.add( "subY" );
        assertEquals( 4, indexer.retrieve( 0, System.currentTimeMillis(), types, subTypes, 0, 100, null ).size() );
    }

    public void testSearchByCount()
        throws Exception
    {
        for ( int i = 0; i < 50; i++ )
        {
            indexer.add( createTimelineRecord() );
        }

        int count = 50;
        assertEquals( count, indexer.retrieve( 0, System.currentTimeMillis(), null, null, 0, count, null ).size() );
        count = 49;
        assertEquals( count, indexer.retrieve( 0, System.currentTimeMillis(), null, null, 0, count, null ).size() );
        count = 25;
        assertEquals( count, indexer.retrieve( 0, System.currentTimeMillis(), null, null, 0, count, null ).size() );
        count = 0;
        assertEquals( count, indexer.retrieve( 0, System.currentTimeMillis(), null, null, 0, count, null ).size() );
        count = 1;
        assertEquals( count, indexer.retrieve( 0, System.currentTimeMillis(), null, null, 0, count, null ).size() );
    }

    public void testSearchByFrom()
        throws Exception
    {
        for ( int i = 0; i < 50; i++ )
        {
            indexer.add( createTimelineRecord() );
        }

        int from = 49;
        assertEquals( 50 - from, indexer.retrieve( 0, System.currentTimeMillis(), null, null, from, 1000, null ).size() );
        from = 1;
        assertEquals( 50 - from, indexer.retrieve( 0, System.currentTimeMillis(), null, null, from, 1000, null ).size() );
        from = 25;
        assertEquals( 50 - from, indexer.retrieve( 0, System.currentTimeMillis(), null, null, from, 1000, null ).size() );
        from = 0;
        assertEquals( 50 - from, indexer.retrieve( 0, System.currentTimeMillis(), null, null, from, 1000, null ).size() );
        from = 50;
        assertEquals( 50 - from, indexer.retrieve( 0, System.currentTimeMillis(), null, null, from, 1000, null ).size() );
    }

    public void testSearchResultOrderByTime()
        throws Exception
    {
        TimelineRecord rec1 = createTimelineRecord();
        rec1.setTimestamp( 1000000L );
        rec1.getData().put( "t", "1" );
        TimelineRecord rec2 = createTimelineRecord();
        rec2.setTimestamp( 2000000L );
        rec2.getData().put( "t", "2" );
        TimelineRecord rec3 = createTimelineRecord();
        rec3.setTimestamp( 3000000L );
        rec3.getData().put( "t", "3" );
        TimelineRecord rec4 = createTimelineRecord();
        rec4.setTimestamp( 4000000L );
        rec4.getData().put( "t", "4" );        

        indexer.add( rec2 );
        indexer.add( rec1 );
        indexer.add( rec4 );
        indexer.add( rec3 );

        List<Map<String, String>> results = indexer.retrieve( 0, System.currentTimeMillis(), null, null, 0, 100, null );

        assertEquals( 4, results.size() );
        assertEquals( "4", results.get( 0 ).get( "t" ) );
        assertEquals( "3", results.get( 1 ).get( "t" ) );
        assertEquals( "2", results.get( 2 ).get( "t" ) );
        assertEquals( "1", results.get( 3 ).get( "t" ) );
    }

    private TimelineRecord createTimelineRecord()
    {
        TimelineRecord record = new TimelineRecord();
        record.setTimestamp( System.currentTimeMillis() );
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
