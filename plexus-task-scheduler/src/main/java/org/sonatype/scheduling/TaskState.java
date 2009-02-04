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
package org.sonatype.scheduling;

/**
 * Enum for describing task state. It is a state machine: starting state is SUBMITTED, finishing states are FINISHED,
 * BROKEN and CANCELLED. Scheduled tasks are jumping between RUNNING and WAITING until finished, cancelled or error
 * (broken).
 * 
 * @author cstamas
 */
public enum TaskState
{
    /**
     * Submitted, not runned yet.
     */
    SUBMITTED, // -> RUNNING, CANCELLED

    /**
     * Is currently running.
     */
    RUNNING, // -> WAITING, FINISHED, BROKEN, CANCELLED, SLEEPING

    /**
     * Should run but is blocked by another clashing task. Will try to run later.
     */
    SLEEPING, // -> RUNNING

    /**
     * Was running and is finished. Waiting for next execution.
     */
    WAITING, // -> RUNNING, CANCELLED

    /**
     * Was running and is finished. No more execution scheduled.
     */
    FINISHED, // END

    /**
     * Was running and is broken.
     */
    BROKEN, // END

    /**
     * Was running and is cancelled.
     */
    CANCELLED; // END
    
    public boolean isActiveOrSubmitted()
    {
        return this.equals( SUBMITTED ) || this.equals( RUNNING ) || this.equals( SLEEPING ) || this.equals( WAITING );
    }
    
    public boolean isActive()
    {
        return this.equals( RUNNING ) || this.equals( SLEEPING ) || this.equals( WAITING );
    }

    public boolean isEndingState()
    {
        /* I don't think BROKEN should apply, broken simply means an exception was thrown.
         * So what?  let the user attempt to do it again, maybe an fs perm problem that they resolved */
        return this.equals( FINISHED ) || this.equals( CANCELLED );
    }
}
