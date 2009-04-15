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

public class TimelinePersistorTest
    extends PlexusTestCase
{
    protected TimelinePersistor persistor;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        persistor = this.lookup( TimelinePersistor.class );

        persistor.configure( new File( PlexusTestCase.getBasedir(), "target/timeline" ) );
    }

    public void testPersistOneRecord()
        throws Exception
    {
        long timestamp = new Date().getTime();
        String type = "type";
        String subType = "subType";
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
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
}
