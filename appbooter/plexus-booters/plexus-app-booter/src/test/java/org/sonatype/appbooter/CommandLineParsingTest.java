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
package org.sonatype.appbooter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Assert;
import junit.framework.TestCase;

public class CommandLineParsingTest
    extends TestCase
{

    public void testCommandLineParsing() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        // hack the private method
        Method testMethod = PlexusContainerHost.class.getDeclaredMethod( "getControlPortFromArgs", new Class[]{String[].class} );
        testMethod.setAccessible( true );
        
        String[] args = null;
        int testValue = (Integer) testMethod.invoke( null, new Object[]{args} );
        Assert.assertEquals( PlexusContainerHost.DEFAULT_CONTROL_PORT, testValue );
        
        args = new String[]{""};
        testValue = (Integer) testMethod.invoke( null, new Object[]{args} );
        Assert.assertEquals( PlexusContainerHost.DEFAULT_CONTROL_PORT, testValue );
        
        args = new String[]{"asdf"};
        testValue = (Integer) testMethod.invoke( null, new Object[]{args} );
        Assert.assertEquals( PlexusContainerHost.DEFAULT_CONTROL_PORT, testValue );
        
        args = new String[]{"-123"};
        testValue = (Integer) testMethod.invoke( null, new Object[]{args} );
        Assert.assertEquals( PlexusContainerHost.DEFAULT_CONTROL_PORT, testValue );
        
        args = new String[]{"123"};
        testValue = (Integer) testMethod.invoke( null, new Object[]{args} );
        Assert.assertEquals( 123, testValue );
        
    }
}
