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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A storage we can store and query timestamped data, based on data type and timestamp. Note: this interface will be
 * shaved off to make it thinner, see NexusTimelime what happened there. Currently, we have a LOT of misleading methods,
 * and most of them are unused.
 * 
 * @author cstamas
 */
public interface Timeline
{
    /**
     * Configures Timeline. It has to be invoked before 1st use.
     * 
     * @param config
     */
    void configure( TimelineConfiguration config )
        throws TimelineException;

    /**
     * Adds a record to the timeline with "now" timestamp (System.currentTimeMillis()).
     * 
     * @param type the record type.
     * @param subType the record subtype.
     * @param data the data to be stored.
     */
    void add( String type, String subType, Map<String, String> data );

    /**
     * Adds multiple records to the timeline, with same "now" timestamp (System.currentTimeMillis()) and same type and
     * subType. Batch add.
     * 
     * @param type the record type.
     * @param subType the record subtype.
     * @param datas the collection of datas for each record.
     */
    void addAll( String type, String subType, Collection<Map<String, String>> datas );

    /**
     * Adds a record to the timeline with provided timestamp.
     * 
     * @param timestamp
     * @param type the record type.
     * @param subType the record subtype.
     * @param data the data to be stored.
     */
    void add( long timestamp, String type, String subType, Map<String, String> data );

    /**
     * Adds multiple records to the timeline, with same provided timestamp and same type and subType. Batch add.
     * 
     * @param timestamp
     * @param type the record type.
     * @param subType the record subtype.
     * @param datas
     */
    void addAll( long timestamp, String type, String subType, Collection<Map<String, String>> datas );

    /**
     * @deprecated Use purgeOlderThan(System.currentTimeMillis(), null, null, null);
     */
    int purgeAll();

    /**
     * @deprecated Use purgeOlderThan(System.currentTimeMillis(), types, null, null);
     */
    int purgeAll( Set<String> types );

    /**
     * @deprecated Use purgeOlderThan(System.currentTimeMillis(), types, subTypes, filter);
     */
    int purgeAll( Set<String> types, Set<String> subTypes, TimelineFilter filter );

    /**
     * @deprecated Use purgeOlderThan(timestamp, null, null, null);
     */
    int purgeOlderThan( long timestamp );

    /**
     * @deprecated Use purgeOlderThan(timestamp, types, null, null);
     */
    int purgeOlderThan( long timestamp, Set<String> types );

    /**
     * Deletes records from timeline that are older than timestamp, and has types (or null for ALL), and has subtypes
     * (or null for ALL) and suits the filter (or null for no filtering).
     * 
     * @param timestamp the timestamp to which compared older records should be deleted.
     * @param types the types that should be deleted, or null for all
     * @param subTypes the subtypes that should be deleted, or null for all.
     * @param filter the filter that should be applied before deletion, or null for no filtering.
     * @return the record count deleted from timeline.
     */
    int purgeOlderThan( long timestamp, Set<String> types, Set<String> subTypes, TimelineFilter filter );

    /**
     * @deprecated Use retrieve(0, count, types, null, null);
     */
    TimelineResult retrieveNewest( int count, Set<String> types );

    /**
     * @deprecated Use retrieve(0, count, types, subtypes, filter);
     */
    TimelineResult retrieveNewest( int count, Set<String> types, Set<String> subtypes, TimelineFilter filter );

    /**
     * @deprecated Querying from timestamp is not reliable, since it depends on Resolution used by TimelineIndexer.
     */
    TimelineResult retrieve( long fromTs, int count, Set<String> types );

    /**
     * @deprecated Querying from timestamp is not reliable, since it depends on Resolution used by TimelineIndexer.
     */
    TimelineResult retrieve( long fromTs, int count, Set<String> types, Set<String> subtypes, TimelineFilter filter );

    /**
     * @deprecated Use retrieve(fromItem, count, types, null, null);
     */
    TimelineResult retrieve( int fromItem, int count, Set<String> types );

    /**
     * Retrieves records from timeline. The order is desceding, newest is 1st, oldest last.
     * 
     * @param fromItem the number of items you want to skip (paging), 0 for none ("from beginning").
     * @param count the count of records you want to retrieve.
     * @param types the types you want to retrieve, or null for all.
     * @param subtypes the subtypes you want to retrieve, or null for all.
     * @param filter the filter you want to apply, or null for no filtering.
     * @return The iterable TimelineResult.
     */
    TimelineResult retrieve( int fromItem, int count, Set<String> types, Set<String> subtypes, TimelineFilter filter );

}
