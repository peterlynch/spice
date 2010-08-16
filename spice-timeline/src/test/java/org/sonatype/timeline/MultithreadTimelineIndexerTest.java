package org.sonatype.timeline;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.timeline.DefaultTimelineIndexer.IndexerTimelineResult;

public class MultithreadTimelineIndexerTest
    extends AbstractTimelineTestCase
{
    protected File indexDirectory;

    protected static final Random rnd = new Random();

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        indexDirectory = new File( PlexusTestCase.getBasedir(), "target/index" );

        cleanDirectory( indexDirectory );

        indexer.configure( new TimelineConfiguration( null, indexDirectory ) );
    }

    public void testKindaNexus()
        throws Exception
    {
        // nexus has deploys and searches happening same time
        // so start 6 deployer (content change in nexus) threads and 2 searcher (RSS fetching) threads

        DeployerThread dt1 =
            new DeployerThread( indexer, new TimelineRecord( System.currentTimeMillis(), "DT1", "1",
                new HashMap<String, String>() ) );
        DeployerThread dt2 =
            new DeployerThread( indexer, new TimelineRecord( System.currentTimeMillis(), "DT2", "1",
                new HashMap<String, String>() ) );
        DeployerThread dt3 =
            new DeployerThread( indexer, new TimelineRecord( System.currentTimeMillis(), "DT3", "1",
                new HashMap<String, String>() ) );
        DeployerThread dt4 =
            new DeployerThread( indexer, new TimelineRecord( System.currentTimeMillis(), "DT4", "1",
                new HashMap<String, String>() ) );
        DeployerThread dt5 =
            new DeployerThread( indexer, new TimelineRecord( System.currentTimeMillis(), "DT5", "1",
                new HashMap<String, String>() ) );
        DeployerThread dt6 =
            new DeployerThread( indexer, new TimelineRecord( System.currentTimeMillis(), "DT6", "1",
                new HashMap<String, String>() ) );

        SearcherThread st1 = new SearcherThread( indexer, "DT1" );
        SearcherThread st2 = new SearcherThread( indexer, "DT3" );

        dt1.start();
        dt2.start();
        dt3.start();
        dt4.start();
        dt5.start();
        dt6.start();

        st1.start();
        st2.start();

        Thread.sleep( 20000 );

        // kill'em
        dt1.interrupt();
        dt2.interrupt();
        dt3.interrupt();
        dt4.interrupt();
        dt5.interrupt();
        dt6.interrupt();

        // stop them nicely (to pick up last dt thread changes)
        st1.stopAndJoin();
        st2.stopAndJoin();

        assertEquals( "DT1 is not fine!", null, unravelThrowable( dt1.getLastException() ) );
        assertEquals( "DT2 is not fine!", null, unravelThrowable( dt2.getLastException() ) );
        assertEquals( "DT3 is not fine!", null, unravelThrowable( dt3.getLastException() ) );
        assertEquals( "DT4 is not fine!", null, unravelThrowable( dt4.getLastException() ) );
        assertEquals( "DT5 is not fine!", null, unravelThrowable( dt5.getLastException() ) );
        assertEquals( "DT6 is not fine!", null, unravelThrowable( dt6.getLastException() ) );

        assertEquals( "ST1 is not fine!", null, unravelThrowable( st1.getLastException() ) );
        assertEquals( "ST2 is not fine!", null, unravelThrowable( st2.getLastException() ) );

        // correctness check
        assertEquals( "Added should equal to found ones", dt1.getAdded(), st1.getLastCount() );
        assertEquals( "Added should equal to found ones", dt3.getAdded(), st2.getLastCount() );
    }

    protected String unravelThrowable( Throwable e )
    {
        if ( e == null )
        {
            return null;
        }
        else
        {
            final Writer result = new StringWriter();

            final PrintWriter printWriter = new PrintWriter( result );

            e.printStackTrace( printWriter );

            return result.toString();
        }
    }

    private static class DeployerThread
        extends Thread
    {
        private final TimelineIndexer timelineIndexer;

        private final TimelineRecord timelineRecord;

        private int added;

        private Throwable ex;

        public DeployerThread( TimelineIndexer timelineIndexer, TimelineRecord timelineRecord )
        {
            this.timelineIndexer = timelineIndexer;

            this.timelineRecord = timelineRecord;

            this.added = 0;

            this.ex = null;
        }

        public void run()
        {
            try
            {
                while ( true )
                {
                    timelineRecord.setTimestamp( System.currentTimeMillis() );

                    timelineIndexer.add( timelineRecord );

                    added++;

                    sleep( Math.abs( rnd.nextLong() ) % 500 );
                }
            }
            catch ( InterruptedException e )
            {
                // we don't care, probably we've been interrupted
            }
            catch ( Exception e )
            {
                ex = e;
            }
        }

        public int getAdded()
        {
            return added;
        }

        public Throwable getLastException()
        {
            return ex;
        }
    }

    private static class SearcherThread
        extends Thread
    {
        private final TimelineIndexer timelineIndexer;

        private final String typeToSearchFor;

        private boolean running;

        private int lastCount = 0;

        private Throwable ex;

        public SearcherThread( TimelineIndexer timelineIndexer, String typeToSearchFor )
        {
            this.timelineIndexer = timelineIndexer;

            this.typeToSearchFor = typeToSearchFor;

            this.running = true;
        }

        public void stopAndJoin()
            throws InterruptedException
        {
            this.running = false;

            join();
        }

        public void run()
        {
            try
            {
                // this one cycle is "inversed": first sleeps then does the work
                // this is needed to have proper "ending", and pick up all the latest changes when stopping
                do
                {
                    sleep( Math.abs( rnd.nextLong() ) % 500 );

                    TimelineResult result =
                        timelineIndexer.retrieve( 0L, System.currentTimeMillis(),
                            Collections.singleton( typeToSearchFor ), null, 0, Integer.MAX_VALUE, null );

                    if ( result instanceof IndexerTimelineResult )
                    {
                        if ( lastCount <= ( (IndexerTimelineResult) result ).getLength() )
                        {
                            // all fine
                            lastCount = ( (IndexerTimelineResult) result ).getLength();
                        }
                        else
                        {
                            throw new IllegalStateException( "We got error!" );
                        }
                    }
                    else
                    {
                        // is empty still
                    }
                }
                while ( running );
            }
            catch ( InterruptedException e )
            {
                // we don't care, probably we've been interrupted
            }
            catch ( Exception e )
            {
                ex = e;
            }
        }

        public int getLastCount()
        {
            return lastCount;
        }

        public Throwable getLastException()
        {
            return ex;
        }
    }
}
