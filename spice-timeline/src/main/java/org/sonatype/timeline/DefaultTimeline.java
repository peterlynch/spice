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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

@Component( role = Timeline.class )
public class DefaultTimeline
    extends AbstractLogEnabled
    implements Timeline
{
    @Requirement
    private TimelinePersistor persistor;

    @Requirement
    private TimelineIndexer indexer;

    private TimelineConfiguration configuration;

    public void configure( TimelineConfiguration config )
        throws TimelineException
    {
        this.configuration = config;

        persistor.configure( config.getPersistDirectory(), config.getPersistRollingInterval() );

        try
        {
            indexer.configure( config.getIndexDirectory() );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Trying to read a record from timeline." );
            }

            indexer.retrieve( 0, System.currentTimeMillis(), null, null, 0, 1, null );
        }
        catch ( TimelineException e )
        {
            getLogger().info( "Failed to configure timeline index, trying to repair it." );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Cause:", e );
            }

            repairTimelineIndexer();

            getLogger().info( "Timeline index is repaired." );
        }

    }

    private void repairTimelineIndexer()
        throws TimelineException
    {
        try
        {
            FileUtils.cleanDirectory( configuration.getIndexDirectory() );
        }
        catch ( IOException e )
        {
            throw new TimelineException( "Failed to clean index directory: "
                + configuration.getIndexDirectory().getAbsolutePath(), e );
        }

        indexer.configure( configuration.getIndexDirectory() );

        for ( TimelineRecord record : persistor.readAll() )
        {
            indexer.add( record );
        }
    }

    public void add( long timestamp, String type, String subType, Map<String, String> data )
    {
        TimelineRecord record = new TimelineRecord( timestamp, type, subType, data );

        try
        {
            persistor.persist( record );

            indexer.add( record );
        }
        catch ( TimelineException e )
        {
            getLogger().warn( "Failed to add a timeline record", e );
        }
    }

    public void add( String type, String subType, Map<String, String> data )
    {
        add( System.currentTimeMillis(), type, subType, data );
    }

    public void addAll( String type, String subType, Collection<Map<String, String>> datas )
    {
        addAll( System.currentTimeMillis(), type, subType, datas );
    }

    public void addAll( long timestamp, String type, String subType, Collection<Map<String, String>> datas )
    {
        for ( Map<String, String> data : datas )
        {
            add( timestamp, type, subType, data );
        }
    }

    private int purge( long fromTime, long toTime, Set<String> types, Set<String> subTypes )
    {
        try
        {
            return indexer.purge( fromTime, toTime, types, subTypes );
        }
        catch ( TimelineException e )
        {
            getLogger().warn( "Failed to purge timeline!", e );

            return 0;
        }
    }

    public int purgeAll()
    {
        return purgeAll( null );
    }

    public int purgeAll( Set<String> types )
    {
        return purgeAll( types, null, null );
    }

    /**
     * Note that currently the filter is not used
     */
    public int purgeAll( Set<String> types, Set<String> subTypes, TimelineFilter filter )
    {
        return purge( 0L, System.currentTimeMillis(), types, subTypes );
    }

    public int purgeOlderThan( long timestamp )
    {
        return purgeOlderThan( timestamp, null );
    }

    public int purgeOlderThan( long timestamp, Set<String> types )
    {
        return purgeOlderThan( timestamp, types, null, null );
    }

    /**
     * Note that currently the filter is not used
     */
    public int purgeOlderThan( long timestamp, Set<String> types, Set<String> subTypes, TimelineFilter filter )
    {
        return purge( 0L, timestamp, types, subTypes );
    }

    public List<Map<String, String>> retrieve( long fromTs, int count, Set<String> types )
    {
        return retrieve( fromTs, count, types, null, null );
    }

    public List<Map<String, String>> retrieve( long fromTs, int count, Set<String> types, Set<String> subTypes,
        TimelineFilter filter )
    {
        return retrieve( fromTs, System.currentTimeMillis(), types, subTypes, 0, count, filter );
    }

    public List<Map<String, String>> retrieve( int fromItem, int count, Set<String> types )
    {
        return retrieve( fromItem, count, types, null, null );
    }

    public List<Map<String, String>> retrieve( int fromItem, int count, Set<String> types, Set<String> subTypes,
        TimelineFilter filter )
    {
        return retrieve( 0L, System.currentTimeMillis(), types, subTypes, fromItem, count, filter );
    }

    private List<Map<String, String>> retrieve( long fromTime, long toTime, Set<String> types, Set<String> subTypes,
        int from, int count, TimelineFilter filter )
    {
        try
        {
            return indexer.retrieve( fromTime, toTime, types, subTypes, from, count, filter );
        }
        catch ( TimelineException e )
        {
            getLogger().warn( "Unable to retrieve data from timeline!", e );

            return new ArrayList<Map<String, String>>();
        }
    }

    public List<Map<String, String>> retrieveNewest( int count, Set<String> types )
    {
        return retrieveNewest( count, types, null, null );
    }

    public List<Map<String, String>> retrieveNewest( int count, Set<String> types, Set<String> subTypes,
        TimelineFilter filter )
    {
        return retrieve( 0L, System.currentTimeMillis(), types, subTypes, 0, count, filter );
    }

}
