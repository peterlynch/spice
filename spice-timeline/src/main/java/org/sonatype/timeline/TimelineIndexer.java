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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Timeline indexer, that adds timeline records to the index to make them quickly retrievable. This component should not
 * be used by users, see Timeline for that.
 * 
 * @author cstamas
 */
public interface TimelineIndexer
{
    /**
     * Configures this component.
     * 
     * @param configuration
     * @throws TimelineException
     */
    void configure( TimelineConfiguration configuration )
        throws TimelineException;

    /**
     * Adds one record to timeline index.
     * 
     * @param record
     * @throws TimelineException
     */
    void add( TimelineRecord record )
        throws TimelineException;

    /**
     * Adds batch of timeline records.
     * 
     * @param records
     * @throws TimelineException
     */
    void add( Iterable<TimelineRecord> records )
        throws TimelineException;

    /**
     * Do a search to the indexed timeline records
     * 
     * @param fromTime from what time to search
     * @param toTime to what time to search
     * @param types which types to search
     * @param subTypes which subTypes to search
     * @param from form which index to return results
     * @param count max number to return
     * @param filter used to filter the result
     * @return search result, ordered by timestamp (the minimum unit is minute)
     */
    List<Map<String, String>> retrieve( long fromTime, long toTime, Set<String> types, Set<String> subTypes, int from,
                                        int count, TimelineFilter filter )
        throws TimelineException;

    /**
     * Purge the indexed timeline records
     * 
     * @param fromTime from what time to purge
     * @param toTime to what time to purge
     * @param types which types to purge
     * @param subTypes which subType to purge
     * @return the number of purged records
     */
    int purge( long fromTime, long toTime, Set<String> types, Set<String> subTypes )
        throws TimelineException;
}
