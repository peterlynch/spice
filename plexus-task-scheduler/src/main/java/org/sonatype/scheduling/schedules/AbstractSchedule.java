/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.scheduling.schedules;

import java.util.Date;

import org.sonatype.scheduling.iterators.SchedulerIterator;

public abstract class AbstractSchedule
    implements Schedule
{
    private final Date startDate;

    private final Date endDate;

    private SchedulerIterator schedulerIterator;

    public AbstractSchedule( Date startDate, Date endDate )
    {
        super();

        this.startDate = startDate;

        this.endDate = endDate;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public SchedulerIterator getIterator()
    {
        if ( schedulerIterator == null )
        {
            schedulerIterator = createIterator();
        }

        return schedulerIterator;
    }

    protected abstract SchedulerIterator createIterator();
}
