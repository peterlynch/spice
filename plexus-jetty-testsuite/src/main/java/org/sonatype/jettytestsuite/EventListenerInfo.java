package org.sonatype.jettytestsuite;

import java.util.EventListener;

public class EventListenerInfo
{
    private String clazz;

    String getClazz()
    {
        return clazz;
    }

    void setClazz( String clazz )
    {
        this.clazz = clazz;
    }

    public EventListener getEventListener() throws Exception
    {
        Class<EventListener> result = (Class<EventListener>) Class.forName( this.clazz );
        
        return result.newInstance();
    }
    
}
