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
package org.sonatype.plexus.jetty;

import org.mortbay.component.LifeCycle;
import org.mortbay.component.LifeCycle.Listener;

public class TestLifeCycleListener
    implements Listener
{
    
    public static boolean startingCalled = false;

    public void lifeCycleFailure( LifeCycle event, Throwable cause )
    {
    }

    public void lifeCycleStarted( LifeCycle event )
    {
    }

    public void lifeCycleStarting( LifeCycle event )
    {
        StackTraceElement element = new Throwable().getStackTrace()[0];
        System.out.println( element.getClassName() + "." + element.getMethodName() + " called." );
        startingCalled = true;
    }

    public void lifeCycleStopped( LifeCycle event )
    {
    }

    public void lifeCycleStopping( LifeCycle event )
    {
    }

}
