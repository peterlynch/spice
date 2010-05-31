package org.sonatype.timeline;

import java.util.Iterator;

public abstract class TimelineResult
    implements Iterable<TimelineRecord>, Iterator<TimelineRecord>
{
    public static final TimelineResult EMPTY_RESULT = new TimelineResult()
    {
        @Override
        public TimelineRecord fetchNextRecord()
        {
            return null;
        }
    };

    // ==

    private TimelineRecord nextRecord;

    private boolean firstCall;

    public TimelineResult()
    {
        this.firstCall = true;
    }

    public Iterator<TimelineRecord> iterator()
    {
        return this;
    }

    protected void init()
    {
        nextRecord = fetchNextRecord();

        firstCall = false;
    }

    public boolean hasNext()
    {
        if ( firstCall )
        {
            init();
        }

        return nextRecord != null;
    }

    public TimelineRecord next()
    {
        if ( firstCall )
        {
            init();
        }

        TimelineRecord result = nextRecord;

        nextRecord = fetchNextRecord();

        return result;
    }

    public void remove()
    {
        throw new UnsupportedOperationException( "This opeation is not supported on TimelineResult!" );
    }

    // ==

    protected abstract TimelineRecord fetchNextRecord();
}
