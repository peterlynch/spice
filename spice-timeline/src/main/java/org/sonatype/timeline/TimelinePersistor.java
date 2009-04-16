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
import java.util.List;

/**
 * Persist the TimelineRecord
 * 
 * @author juven
 */
public interface TimelinePersistor
{
    void configure( File persistDirectory );

    /**
     * TimelinePersistor must be configured before being used.
     * @param persistDirectory the place to put the persistent file
     * @param rollingInterval the interval for rolling the persistent file (seconds)
     */
    public void configure( File persistDirectory, int rollingInterval );
    
    void persist( TimelineRecord record )
        throws TimelineException;

    List<TimelineRecord> readAll()
        throws TimelineException;
}
