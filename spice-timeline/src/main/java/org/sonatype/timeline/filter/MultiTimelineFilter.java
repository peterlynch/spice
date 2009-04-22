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

import java.util.ArrayList;
import java.util.List;

import org.sonatype.timeline.TimelineFilter;

public abstract class MultiTimelineFilter
    implements TimelineFilter
{
    private final List<TimelineFilter> terms;

    public MultiTimelineFilter()
    {
        this.terms = new ArrayList<TimelineFilter>();
    }

    public MultiTimelineFilter( List<TimelineFilter> terms )
    {
        this.terms = terms;
    }

    public void addTerm( TimelineFilter filter )
    {
        terms.add( filter );
    }

    protected List<TimelineFilter> getTerms()
    {
        return terms;
    }
}
