package org.sonatype.plexus.appevents;

import java.util.Date;

public interface Event<T>
{
    Date getEventDate();
    
    T getEventSender();
}
