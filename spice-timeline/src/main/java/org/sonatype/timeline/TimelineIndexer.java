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

import java.util.Set;

public interface TimelineIndexer
{
    void configure( TimelineConfiguration config )
        throws TimelineException;

    void add( TimelineRecord record )
        throws TimelineException;

    void addAll( TimelineResult records )
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
    TimelineResult retrieve( long fromTime, long toTime, Set<String> types, Set<String> subTypes, int from, int count,
                             TimelineFilter filter )
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
