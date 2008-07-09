package org.sonatype.appbooter;

import java.io.IOException;
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
