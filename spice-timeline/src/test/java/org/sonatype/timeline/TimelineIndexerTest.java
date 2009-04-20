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
        long timestamp = new Date().getTime();
        String type = "type";
        String subType = "subType";
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1 test", "v1" );
        data.put( "k2", "v2" );

        TimelineRecord record = new TimelineRecord();
        record.setTimestamp( timestamp );
        record.setType( type );
        record.setSubType( subType );
        record.setData( data );

        indexer.add( record );

        Set<String> types = new HashSet<String>();
        types.add( "type" );
        List<Map<String, String>> results = indexer.retrieve( 0, System.currentTimeMillis(), types, null, 0, 100, null );

        assertEquals( 1, results.size() );
        assertEquals( data, results.get( 0 ) );
    }
}
