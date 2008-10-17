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
package org.sonatype.scheduling;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

import org.sonatype.scheduling.schedules.Schedule;

public interface Scheduler
{
    void startService()
        throws Exception;

    void stopService()
        throws Exception;

    /**
     * Initialize a task on bootup.
     * 
     * @param name
     * @param runnable
     * @param schedule
     * @param taskParams
     * @param store
     * @return
     */
    <T> ScheduledTask<T> initialize( String id, String name, String type, Callable<T> callable, Schedule schedule,
        Map<String, String> taskParams )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for immediate execution, but have a control over it.
     * 
     * @param name
     * @param runnable
     * @param taskParams
     * @return
     */
    ScheduledTask<Object> submit( String name, Runnable runnable, Map<String, String> taskParams )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param name
     * @param runnable
     * @param schedule
     * @param taskParams
     * @param store
     * @return
     */
    ScheduledTask<Object> schedule( String name, Runnable runnable, Schedule schedule, Map<String, String> taskParams )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Callable for immediate execution, but have a control over it.
     * 
     * @param name
     * @param runnable
     * @param taskParams
     * @return
     */
    <T> ScheduledTask<T> submit( String name, Callable<T> callable, Map<String, String> taskParams )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param name
     * @param runnable
     * @param schedule
     * @param taskParams
     * @param store
     * @return
     */
    <T> ScheduledTask<T> schedule( String name, Callable<T> callable, Schedule schedule, Map<String, String> taskParams )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Issue a Runnable for scheduled execution.
     * 
     * @param task
     * @return
     */
    <T> ScheduledTask<T> updateSchedule( ScheduledTask<T> task )
        throws RejectedExecutionException,
            NullPointerException;

    /**
     * Returns the map of currently active tasks. The resturned collection is an unmodifiable snapshot. It may differ
     * from current one (if some thread finishes for example during processing of the returned list).
     * 
     * @return
     */
    Map<String, List<ScheduledTask<?>>> getActiveTasks();

    /**
     * Returns the map of all tasks. The resturned collection is an unmodifiable snapshot. It may differ from current
     * one (if some thread finishes for example during processing of the returned list).
     * 
     * @return
     */
    Map<String, List<ScheduledTask<?>>> getAllTasks();

    /**
     * Returns an active task by it's ID.
     * 
     * @param id
     * @return
     */
    ScheduledTask<?> getTaskById( String id )
        throws NoSuchTaskException;

    /**
     * A factory for tasks.
     * 
     * @param taskType
     * @return
     * @throws IllegalArgumentException
     */
    SchedulerTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException;

    /**
     * A factory for tasks.
     * 
     * @param taskType
     * @return
     * @throws IllegalArgumentException
     */
    SchedulerTask<?> createTaskInstance( Class<?> taskType )
        throws IllegalArgumentException;
}
