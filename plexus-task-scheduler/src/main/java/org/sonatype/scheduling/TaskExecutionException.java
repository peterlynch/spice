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
package org.sonatype.scheduling;

/**
 * Exception used to wrap-up the non-exceptions (errors like OOM) occured during runtime of tasks into an Exception.
 * 
 * @author cstamas
 */
public class TaskExecutionException
    extends Exception
{
    public TaskExecutionException( String message )
    {
        super( message );
    }

    public TaskExecutionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public TaskExecutionException( Throwable cause )
    {
        super( cause );
    }
}
