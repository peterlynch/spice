/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.lifecycle;

import junit.framework.TestCase;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.PhaseExecutionException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

public class LifeCycleTest
    extends TestCase
{
    public void testStartingException()
    {
        assertNull( new StartingException( (String) null ).getMessage() );
        assertNull( new StartingException( (Throwable) null ).getMessage() );
        assertNull( new StartingException( null, new NullPointerException() ).getMessage() );
    }

    public void testStoppingException()
    {
        assertNull( new StoppingException( (String) null ).getMessage() );
        assertNull( new StoppingException( null, new NullPointerException() ).getMessage() );
    }

    public void testPhaseExecutionException()
    {
        assertNull( new PhaseExecutionException( null, new NullPointerException() ).getMessage() );
    }
}
