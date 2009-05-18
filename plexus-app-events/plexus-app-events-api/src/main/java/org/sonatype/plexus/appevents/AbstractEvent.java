/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.plexus.appevents;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The superclass for all events.
 * 
 * @author cstamas
 */
public abstract class AbstractEvent<T>
    implements Event<T>
{
    /** The event date. */
    private final Date eventDate;

    /** The event context */
    private final HashMap<Object, Object> eventContext;

    /** The sender */
    private final T eventSender;

    /**
     * Instantiates a new abstract event.
     */
    public AbstractEvent( T component )
    {
        super();

        this.eventDate = new Date();

        this.eventContext = new HashMap<Object, Object>();

        this.eventSender = component;
    }

    /**
     * Gets the event date.
     * 
     * @return the event date
     */
    public Date getEventDate()
    {
        return eventDate;
    }

    /**
     * Gets the modifiable event context.
     * 
     * @return the event context
     */
    public Map<Object, Object> getEventContext()
    {
        return eventContext;
    }

    /**
     * Gets the sender
     * 
     * @return the event sender
     */
    public T getEventSender()
    {
        return eventSender;
    }
}
