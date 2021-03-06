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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

public class TimelineTest
    extends AbstractTimelineTestCase
{
    protected File persistDirectory;

    protected File indexDirectory;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        persistDirectory = new File( PlexusTestCase.getBasedir(), "target/persist" );
        cleanDirectory( persistDirectory );
        indexDirectory = new File( PlexusTestCase.getBasedir(), "target/index" );
        cleanDirectory( indexDirectory );
    }

    public void testConfigureTimeline()
        throws Exception
    {
        timeline.configure( new TimelineConfiguration( persistDirectory, indexDirectory ) );
    }

    public void testSimpleAddAndRetrieve()
        throws Exception
    {
        timeline.configure( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
        data.put( "k2", "v2" );
        data.put( "k3", "v3" );
        timeline.add( "typeA", "subType", data );

        Set<String> types = new HashSet<String>();
        types.add( "typeA" );
        List<TimelineRecord> results = asList( timeline.retrieveNewest( 10, types ) );

        assertEquals( 1, results.size() );
        assertEquals( data, results.get( 0 ).getData() );
    }

    public void testPurge()
        throws Exception
    {
        timeline.configure( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        String type = "type";
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );

        timeline.add( 1000000L, type, null, data );
        timeline.add( 2000000L, type, null, data );
        timeline.add( 3000000L, type, null, data );
        timeline.add( 4000000L, type, null, data );

        assertEquals( 4, sizeOf( timeline.retrieve( 0, 10, null ) ) );
        assertEquals( 3, timeline.purgeOlderThan( 3500000L ) );
        assertEquals( 1, sizeOf( timeline.retrieve( 0, 10, null ) ) );
        assertEquals( 1, timeline.purgeAll() );
        assertEquals( 0, sizeOf( timeline.retrieve( 0, 10, null ) ) );
    }

    public void testRepairIndexCouldNotRead()
        throws Exception
    {
        // here we use data produced by testSimpleAddAndRetrieve(), but the index file is crashed (manually edited)
        File crashedPersistDir =
            new File( PlexusTestCase.getBasedir(), "target/test-classes/crashed-could-not-read/persist" );
        File carshedIndexDir =
            new File( PlexusTestCase.getBasedir(), "target/test-classes/crashed-could-not-read/index" );
        FileUtils.copyDirectoryStructure( crashedPersistDir, persistDirectory );
        FileUtils.copyDirectoryStructure( carshedIndexDir, indexDirectory );

        timeline.configure( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
        data.put( "k2", "v2" );
        data.put( "k3", "v3" );

        Set<String> types = new HashSet<String>();
        types.add( "typeA" );
        List<TimelineRecord> results = asList( timeline.retrieveNewest( 10, types ) );

        assertEquals( 1, results.size() );
        assertEquals( data, results.get( 0 ).getData() );
    }

    public void testRepairIndexCouldNotRetrieve()
        throws Exception
    {
        File crashedPersistDir =
            new File( PlexusTestCase.getBasedir(), "target/test-classes/crashed-could-not-retrieve/persist" );
        File carshedIndexDir =
            new File( PlexusTestCase.getBasedir(), "target/test-classes/crashed-could-not-retrieve/index" );
        FileUtils.copyDirectoryStructure( crashedPersistDir, persistDirectory );
        FileUtils.copyDirectoryStructure( carshedIndexDir, indexDirectory );

        timeline.configure( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        assertTrue( sizeOf( timeline.retrieveNewest( 10, null ) ) > 0 );
    }

    public void testRepairIndexCouldNotAdd()
        throws Exception
    {
        File persistDir = new File( PlexusTestCase.getBasedir(), "target/test-classes/crashed-could-not-add/persist" );
        File goodIndexDir =
            new File( PlexusTestCase.getBasedir(), "target/test-classes/crashed-could-not-add/index-good" );
        File crashedIndexDir =
            new File( PlexusTestCase.getBasedir(), "target/test-classes/crashed-could-not-add/index-broken" );
        FileUtils.copyDirectoryStructure( persistDir, persistDirectory );
        FileUtils.copyDirectoryStructure( goodIndexDir, indexDirectory );

        timeline.configure( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        // pretend that when timeline is running, the index is manually changed
        cleanDirectory( indexDirectory );
        FileUtils.copyDirectoryStructure( crashedIndexDir, indexDirectory );

        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
        data.put( "k2", "v2" );
        data.put( "k3", "v3" );
        timeline.add( "typeA", "subType", data );
    }

    public void testRepairIndexCouldNotPurge()
        throws Exception
    {
        File persistDir = new File( PlexusTestCase.getBasedir(), "target/test-classes/crashed-could-not-purge/persist" );
        File goodIndexDir =
            new File( PlexusTestCase.getBasedir(), "target/test-classes/crashed-could-not-purge/index-good" );
        File crashedIndexDir =
            new File( PlexusTestCase.getBasedir(), "target/test-classes/crashed-could-not-purge/index-broken" );
        FileUtils.copyDirectoryStructure( persistDir, persistDirectory );
        FileUtils.copyDirectoryStructure( goodIndexDir, indexDirectory );

        timeline.configure( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        // pretend that when timeline is running, the index is manually changed
        cleanDirectory( indexDirectory );
        FileUtils.copyDirectoryStructure( crashedIndexDir, indexDirectory );

        assertTrue( timeline.purgeAll() > 0 );
    }
}
