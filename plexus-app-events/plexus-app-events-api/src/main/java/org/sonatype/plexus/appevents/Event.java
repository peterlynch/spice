package org.sonatype.plexus.appevents;

import java.util.Date;

public interface Event
{
    Date getEventDate();
    
    Object getEventSender();
}
