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
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.PlexusTestCase;

public abstract class AbstractTimelineTestCase
    extends PlexusTestCase
{
    protected Timeline timeline;

    protected TimelinePersistor persistor;

    protected TimelineIndexer indexer;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        timeline = this.lookup( Timeline.class );
        persistor = this.lookup( TimelinePersistor.class );
        indexer = this.lookup( TimelineIndexer.class );
    }

    protected void cleanDirectory( File directory )
        throws Exception
    {
        if ( directory.exists() )
        {
            for ( File file : directory.listFiles() )
            {
                file.delete();
            }
            directory.delete();
        }
    }

    protected TimelineRecord createTimelineRecord()
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
        data.put( "k2", "v2" );
        data.put( "k3", "v3" );

        return new TimelineRecord( System.currentTimeMillis(), "type", "subType", data );
    }
}
