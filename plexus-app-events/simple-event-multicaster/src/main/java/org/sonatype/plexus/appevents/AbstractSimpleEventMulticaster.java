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

import java.util.concurrent.CopyOnWriteArrayList;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * Simple implementation. The Class ProximityEventMulticasterComponent implements multicasting. Used by Repository and
 * Registry implementations. Note: this class methods are dedicated to Proximity :)
 * 
 * @author cstamas
 */
public abstract class AbstractSimpleEventMulticaster
    extends AbstractLogEnabled
{
    /** The proximity event listeners. */
    private CopyOnWriteArrayList<EventListener> proximityEventListeners = new CopyOnWriteArrayList<EventListener>();

    public void addEventListener( EventListener listener )
    {
        proximityEventListeners.add( listener );
    }

    public void removeEventListener( EventListener listener )
    {
        proximityEventListeners.remove( listener );
    }

    public void notifyEventListeners( Event evt )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                               "Notifying " + proximityEventListeners.size() + " EventListener about event "
                                   + evt.getClass().getName() + " fired (" + evt.toString() + ")" );
        }

        for ( EventListener l : proximityEventListeners )
        {
            try
            {
                l.onEvent( evt );
            }
            catch ( Exception e )
            {
                getLogger().info( "Unexpected exception in listener, continuing listener notification.", e );
            }
        }
    }
}
