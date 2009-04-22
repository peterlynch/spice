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
package org.sonatype.timeline.filter;

import java.util.List;
import java.util.Map;

import org.sonatype.timeline.TimelineFilter;

public class AndTimelineFilter
    extends MultiTimelineFilter
{
    public AndTimelineFilter()
    {
        super();
    }

    public AndTimelineFilter( List<TimelineFilter> terms )
    {
        super( terms );
    }

    public boolean accept( Map<String, String> hit )
    {
        for ( TimelineFilter term : getTerms() )
        {
            if ( !term.accept( hit ) )
            {
                return false;
            }
        }

        return true;
    }
}
