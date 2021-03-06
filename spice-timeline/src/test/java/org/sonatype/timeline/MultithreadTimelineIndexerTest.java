package org.sonatype.timeline;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.timeline.DefaultTimelineIndexer.IndexerTimelineResult;

public class MultithreadTimelineIndexerTest
    extends AbstractTimelineTestCase
{
    protected File indexDirectory;

    protected static final Random rnd = new Random();

    // number of deploy threads (and search threads) to launch
    protected static final int THREAD_COUNT = 10;

    // max time the deploy threads will sleep between adding to timeline
    protected static final long MAX_DEPLOY_SLEEP_TIME = 100;

    // max time the search threads will sleep between searches
    protected static final long MAX_SEARCH_SLEEP_TIME = 500;

    // time to sleep between starting threads, and stopping them
    protected static final long SLEEP_TIME = THREAD_COUNT * MAX_DEPLOY_SLEEP_TIME * 10;

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
        // so start 20 deployer (content change in nexus) threads and 10 searcher (RSS fetching) threads

        List<DeployerThread> deployerThreads = new ArrayList<DeployerThread>();

        for ( int i = 0; i < THREAD_COUNT; i++ )
        {
            deployerThreads.add( new DeployerThread( indexer, new TimelineRecord( System.currentTimeMillis(), "DT" + i,
                                                                                  "1", new HashMap<String, String>() ) ) );
        }

        List<SearcherThread> searcherThreads = new ArrayList<SearcherThread>();

        for ( int i = 0; i < THREAD_COUNT; i++ )
        {
            searcherThreads.add( new SearcherThread( indexer, "DT" + ( i ) ) );
        }

        for ( DeployerThread thread : deployerThreads )
        {
            thread.start();
        }

        for ( SearcherThread thread : searcherThreads )
        {
            thread.start();
        }

        Thread.sleep( SLEEP_TIME );

        // kill'em
        for ( DeployerThread thread : deployerThreads )
        {
            thread.stopAndJoin();
        }

        // let the searchers run for more
        Thread.sleep( MAX_SEARCH_SLEEP_TIME );

        // stop them nicely (to pick up last dt thread changes)
        for ( SearcherThread thread : searcherThreads )
        {
            thread.stopAndJoin();
        }

        for ( DeployerThread thread : deployerThreads )
        {
            assertEquals( thread.getTimelineRecord().getType() + " deployer is not fine!", null,
                          unravelThrowable( thread.getLastException() ) );
        }

        for ( SearcherThread thread : searcherThreads )
        {
            assertEquals( thread.getTypeToSearchFor() + " searcher is not fine!", null,
                          unravelThrowable( thread.getLastException() ) );
        }

        for ( int i = 0; i < THREAD_COUNT; i++ )
        {
            assertEquals( "Added should equal to found ones", deployerThreads.get( i ).getAdded(),
                          searcherThreads.get( i ).getLastCount() );
        }
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

        private boolean running;

        public DeployerThread( TimelineIndexer timelineIndexer, TimelineRecord timelineRecord )
        {
            this.timelineIndexer = timelineIndexer;

            this.timelineRecord = timelineRecord;

            this.added = 0;

            this.ex = null;

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
                while ( running )
                {
                    timelineRecord.setTimestamp( System.currentTimeMillis() );

                    timelineIndexer.add( timelineRecord );

                    added++;

                    sleep( Math.abs( rnd.nextLong() ) % MAX_DEPLOY_SLEEP_TIME );
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

        public TimelineRecord getTimelineRecord()
        {
            return timelineRecord;
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
                    sleep( Math.abs( rnd.nextLong() ) % MAX_SEARCH_SLEEP_TIME );

                    TimelineResult result =
                        timelineIndexer.retrieve( 0L, System.currentTimeMillis(),
                                                  Collections.singleton( typeToSearchFor ), null, 0, Integer.MAX_VALUE,
                                                  null );

                    try
                    {

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
                    finally
                    {
                        result.release();
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

        public String getTypeToSearchFor()
        {
            return typeToSearchFor;
        }
    }
}
