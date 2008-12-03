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
