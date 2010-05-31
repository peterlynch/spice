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
 * A place we can store and query data, based on data type and timestamp
 */
public interface Timeline
{
    /**
     * Configure it before use
     * 
     * @param config
     */
    void configure( TimelineConfiguration config )
        throws TimelineException;

    void add( String type, String subType, Map<String, String> data );

    void addAll( String type, String subType, Collection<Map<String, String>> datas );

    void add( long timestamp, String type, String subType, Map<String, String> data );

    void addAll( long timestamp, String type, String subType, Collection<Map<String, String>> datas );

    int purgeAll();

    int purgeAll( Set<String> types );

    int purgeAll( Set<String> types, Set<String> subTypes, TimelineFilter filter );

    int purgeOlderThan( long timestamp );

    int purgeOlderThan( long timestamp, Set<String> types );

    int purgeOlderThan( long timestamp, Set<String> types, Set<String> subTypes, TimelineFilter filter );

    TimelineResult retrieveNewest( int count, Set<String> types );

    TimelineResult retrieveNewest( int count, Set<String> types, Set<String> subtypes, TimelineFilter filter );

    TimelineResult retrieve( long fromTs, int count, Set<String> types );

    TimelineResult retrieve( long fromTs, int count, Set<String> types, Set<String> subtypes, TimelineFilter filter );

    TimelineResult retrieve( int fromItem, int count, Set<String> types );

    TimelineResult retrieve( int fromItem, int count, Set<String> types, Set<String> subtypes, TimelineFilter filter );

}
