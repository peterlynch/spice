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

/**
 * Timeline persistor, that persists the timeline records. This component should not be used by users, see Timeline for
 * that.
 * 
 * @author juven
 * @author cstamas
 */
public interface TimelinePersistor
{
    public static final int DEFAULT_ROLLING_INTERVAL = 60 * 60 * 24;

    /**
     * Configures the persistor. It sets where to persist.
     * 
     * @param persistDirectory
     */
    void configure( TimelineConfiguration config );

    /**
     * Saves one timeline record to persistent store.
     * 
     * @param record
     * @throws TimelineException
     */
    void persist( TimelineRecord record )
        throws TimelineException;

    /**
     * Reads up all records, but using iterator (to keep small memory footprint).
     * 
     * @return
     * @throws TimelineException
     */
    Iterable<TimelineRecord> readAll()
        throws TimelineException;
}
