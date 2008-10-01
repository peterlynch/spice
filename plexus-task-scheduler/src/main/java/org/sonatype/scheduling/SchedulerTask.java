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

public interface SchedulerTask<T>
    extends Callable<T>
{
    String ROLE = SchedulerTask.class.getName();

    boolean allowConcurrentSubmission( Map<String, List<ScheduledTask<?>>> currentActiveTasks );

    boolean allowConcurrentExecution( Map<String, List<ScheduledTask<?>>> currentActiveTasks );

    void addParameter( String key, String value );

    String getParameter( String key );

    Map<String, String> getParameters();
}
