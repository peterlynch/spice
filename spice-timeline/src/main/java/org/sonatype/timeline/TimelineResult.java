package org.sonatype.timeline;

import java.io.IOException;
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
        
        @Override
        protected void doRelease()
            throws IOException
        {   
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
        throw new UnsupportedOperationException( "This operation is not supported on TimelineResult!" );
    }
    
    public void release()
        throws IOException
    {
        doRelease();
    }

    // ==

    protected abstract TimelineRecord fetchNextRecord();
    
    protected abstract void doRelease()
        throws IOException;
}
