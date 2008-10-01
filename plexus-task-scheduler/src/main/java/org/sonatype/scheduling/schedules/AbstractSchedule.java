/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
