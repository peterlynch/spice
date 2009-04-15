package org.sonatype.timeline;

import java.util.List;

public interface TimelineManager
{
    void add( TimelineRecord record );

    void addAll( List<TimelineRecord> records );
    
    
}
