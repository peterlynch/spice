/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.timeline;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

@Component( role = TimelineIndexer.class )
public class DefaultTimelineIndexer
    extends AbstractLogEnabled
    implements TimelineIndexer, Startable
{
    private static final String TIMESTAMP = "_t";

    private static final String TYPE = "_1";

    private static final String SUBTYPE = "_2";

    private static final Resolution TIMELINE_RESOLUTION = Resolution.SECOND;

    private Directory directory;

    private IndexReader indexReader;

    private IndexWriter indexWriter;

    private IndexSearcher indexSearcher;

    public void configure( TimelineConfiguration configuration )
        throws TimelineException
    {
        try
        {
            boolean newIndex = true;

            synchronized ( this )
            {
                if ( directory != null )
                {
                    directory.close();
                }

                directory = FSDirectory.getDirectory( configuration.getIndexDirectory() );

                if ( IndexReader.indexExists( directory ) )
                {
                    if ( IndexWriter.isLocked( directory ) )
                    {
                        IndexWriter.unlock( directory );
                    }

                    newIndex = false;
                }

                indexWriter =
                    new IndexWriter( configuration.getIndexDirectory(), new KeywordAnalyzer(), newIndex,
                        MaxFieldLength.LIMITED );

                closeIndexWriter();
            }
        }
        catch ( Exception e )
        {
            throw new TimelineException( "Fail to configure timeline index!", e );
        }

    }

    public void start()
        throws StartingException
    {
    }

    public void stop()
        throws StoppingException
    {
        try
        {
            closeIndexWriter();

            closeIndexReaderAndSearcher();

            if ( directory != null )
            {
                directory.close();
            }
        }
        catch ( IOException e )
        {
            throw new StoppingException( "Unable to stop Timeline!", e );
        }
    }

    protected IndexWriter getIndexWriter()
        throws IOException
    {
        if ( indexWriter == null )
        {
            indexWriter = new IndexWriter( directory, new KeywordAnalyzer(), false, MaxFieldLength.LIMITED );
        }

        return indexWriter;
    }

    protected void closeIndexWriter()
        throws IOException
    {
        if ( indexWriter != null )
        {
            indexWriter.commit();

            indexWriter.close();

            indexWriter = null;
        }
    }

    protected IndexReader getIndexReader( boolean readOnly )
        throws IOException
    {
        if ( readOnly )
        {
            // just "fire and forget", always create one
            return IndexReader.open( directory, readOnly );
        }
        else
        {
            if ( indexReader == null || !indexReader.isCurrent() )
            {
                if ( indexReader != null )
                {
                    indexReader.close();
                }

                indexReader = IndexReader.open( directory, readOnly );
            }
            return indexReader;
        }
    }

    protected IndexSearcher getIndexSearcher( boolean readOnly )
        throws IOException
    {
        if ( readOnly )
        {
            // just "fire and forget", always create one
            return new IndexSearcher( getIndexReader( readOnly ) );
        }
        else
        {
            if ( indexSearcher == null || getIndexReader( readOnly ) != indexSearcher.getIndexReader() )
            {
                if ( indexSearcher != null )
                {
                    indexSearcher.close();

                    // the reader was supplied explicitly
                    indexSearcher.getIndexReader().close();
                }

                indexSearcher = new IndexSearcher( getIndexReader( readOnly ) );
            }

            return indexSearcher;
        }
    }

    protected void closeIndexReaderAndSearcher()
        throws IOException
    {
        if ( indexSearcher != null )
        {
            indexSearcher.getIndexReader().close();

            indexSearcher.close();

            indexSearcher = null;
        }

        if ( indexReader != null )
        {
            indexReader.close();

            indexReader = null;
        }
    }

    public void add( TimelineRecord record )
        throws TimelineException
    {
        IndexWriter writer = null;

        try
        {
            synchronized ( this )
            {
                writer = getIndexWriter();

                writer.addDocument( createDocument( record ) );

                closeIndexWriter();
            }
        }
        catch ( IOException e )
        {
            throw new TimelineException( "Fail to add a record to the timeline index", e );
        }
    }

    public void addAll( TimelineResult res )
        throws TimelineException
    {
        IndexWriter writer = null;

        try
        {
            synchronized ( this )
            {
                writer = getIndexWriter();

                for ( TimelineRecord rec : res )
                {
                    writer.addDocument( createDocument( rec ) );
                }

                closeIndexWriter();
            }
        }
        catch ( IOException e )
        {
            throw new TimelineException( "Fail to add a record to the timeline index", e );
        }
    }

    private Document createDocument( TimelineRecord record )
    {
        Document doc = new Document();

        doc.add( new Field( TIMESTAMP, DateTools.timeToString( record.getTimestamp(), TIMELINE_RESOLUTION ),
            Field.Store.YES, Field.Index.NOT_ANALYZED ) );

        doc.add( new Field( TYPE, record.getType(), Field.Store.YES, Field.Index.NOT_ANALYZED ) );

        doc.add( new Field( SUBTYPE, record.getSubType(), Field.Store.YES, Field.Index.NOT_ANALYZED ) );

        for ( String key : record.getData().keySet() )
        {
            doc.add( new Field( key, record.getData().get( key ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
        }

        return doc;
    }

    private Query buildQuery( long from, long to, Set<String> types, Set<String> subTypes )
    {
        if ( isEmptySet( types ) && isEmptySet( subTypes ) )
        {
            return new ConstantScoreRangeQuery( TIMESTAMP, DateTools.timeToString( from, TIMELINE_RESOLUTION ),
                DateTools.timeToString( to, TIMELINE_RESOLUTION ), true, true );
        }
        else
        {
            BooleanQuery result = new BooleanQuery();

            result.add( new ConstantScoreRangeQuery( TIMESTAMP, DateTools.timeToString( from, TIMELINE_RESOLUTION ),
                DateTools.timeToString( to, TIMELINE_RESOLUTION ), true, true ), Occur.MUST );

            if ( !isEmptySet( types ) )
            {
                BooleanQuery typeQ = new BooleanQuery();

                for ( String type : types )
                {
                    typeQ.add( new TermQuery( new Term( TYPE, type ) ), Occur.SHOULD );
                }

                result.add( typeQ, Occur.MUST );
            }
            if ( !isEmptySet( subTypes ) )
            {
                BooleanQuery subTypeQ = new BooleanQuery();

                for ( String subType : subTypes )
                {
                    subTypeQ.add( new TermQuery( new Term( SUBTYPE, subType ) ), Occur.SHOULD );
                }

                result.add( subTypeQ, Occur.MUST );
            }
            return result;
        }
    }

    private boolean isEmptySet( Set<String> set )
    {
        return set == null || set.size() == 0;
    }

    public TimelineResult retrieve( long fromTime, long toTime, Set<String> types, Set<String> subTypes, int from,
                                    int count, TimelineFilter filter )
        throws TimelineException
    {
        try
        {
            synchronized ( this )
            {
                IndexSearcher searcher = getIndexSearcher( true );

                if ( searcher.maxDoc() == 0 )
                {
                    searcher.close();

                    searcher.getIndexReader().close();

                    return TimelineResult.EMPTY_RESULT;
                }

                TopFieldDocs topDocs =
                    searcher.search( buildQuery( fromTime, toTime, types, subTypes ), null, searcher.maxDoc(),
                        new Sort( new SortField( TIMESTAMP, SortField.LONG, true ) ) );

                if ( topDocs.scoreDocs.length == 0 )
                {
                    searcher.close();

                    searcher.getIndexReader().close();

                    return TimelineResult.EMPTY_RESULT;
                }

                return new IndexerTimelineResult( searcher, topDocs, filter, from, count );
            }
        }
        catch ( IOException e )
        {
            throw new TimelineException( "Failed to retrieve records from the timeline index!", e );
        }
    }

    public static class IndexerTimelineResult
        extends TimelineResult
    {
        private IndexSearcher searcher;

        private TopFieldDocs hits;

        private TimelineFilter filter;

        private int docNumberToSkip;

        private int docNumberToReturn;

        private int i;

        private int returned;

        public IndexerTimelineResult( final IndexSearcher searcher, final TopFieldDocs hits,
                                      final TimelineFilter filter, final int docNumberToSkip,
                                      final int docNumberToReturn )
        {
            this.searcher = searcher;

            this.hits = hits;

            this.filter = filter;

            this.docNumberToSkip = docNumberToSkip;

            this.docNumberToReturn = docNumberToReturn;

            this.i = 0;

            this.returned = 0;
        }

        protected void close()
        {
            if ( searcher != null )
            {
                try
                {
                    searcher.close();
                }
                catch ( IOException e )
                {
                    // hum hum
                }

                try
                {
                    searcher.getIndexReader().close();
                }
                catch ( IOException e )
                {
                    // uh oh
                }

                searcher = null;
            }
        }

        public void finalize()
            throws Throwable
        {
            super.finalize();

            close();
        }

        @Override
        protected TimelineRecord fetchNextRecord()
        {
            if ( i >= hits.scoreDocs.length || returned >= docNumberToReturn )
            {
                close();

                return null;
            }

            try
            {
                Document doc = searcher.doc( hits.scoreDocs[i++].doc );

                TimelineRecord data = buildData( doc );

                if ( filter != null && !filter.accept( data ) )
                {
                    return fetchNextRecord();
                }

                // skip the unneeded stuff
                // Warning: this means we skip the needed FILTERED stuff out!
                if ( docNumberToSkip > 0 )
                {
                    docNumberToSkip--;

                    return fetchNextRecord();
                }

                returned++;

                return data;
            }
            catch ( IOException e )
            {
                return null;
            }
        }

        @SuppressWarnings( "unchecked" )
        protected TimelineRecord buildData( Document doc )
        {
            long timestamp = -1;

            String tsString = doc.get( TIMESTAMP );

            if ( tsString != null )
            {
                // legacy indexes will have nulls here
                try
                {
                    timestamp = DateTools.stringToTime( tsString );
                }
                catch ( ParseException e )
                {
                    // leave it -1
                }
            }

            // legacy indexes will have nulls here
            String type = doc.get( TYPE );

            // legacy indexes will have nulls here
            String subType = doc.get( SUBTYPE );

            Map<String, String> data = new HashMap<String, String>();

            TimelineRecord result = new TimelineRecord( timestamp, type, subType, data );

            for ( Field field : (List<Field>) doc.getFields() )
            {
                if ( !field.name().startsWith( "_" ) )
                {
                    data.put( field.name(), field.stringValue() );
                }
            }

            return result;
        }
    }

    public int purge( long fromTime, long toTime, Set<String> types, Set<String> subTypes )
        throws TimelineException
    {
        try
        {
            synchronized ( this )
            {
                closeIndexWriter();

                IndexSearcher searcher = getIndexSearcher( false );

                if ( searcher.maxDoc() == 0 )
                {
                    closeIndexReaderAndSearcher();

                    return 0;
                }

                TopFieldDocs topDocs =
                    searcher.search( buildQuery( fromTime, toTime, types, subTypes ), null, searcher.maxDoc(),
                        new Sort( new SortField( TIMESTAMP, SortField.LONG, true ) ) );

                for ( int i = 0; i < topDocs.scoreDocs.length; i++ )
                {
                    searcher.getIndexReader().deleteDocument( topDocs.scoreDocs[i].doc );
                }

                closeIndexReaderAndSearcher();

                getIndexWriter().optimize();

                closeIndexWriter();

                return topDocs.scoreDocs.length;
            }
        }
        catch ( IOException e )
        {
            throw new TimelineException( "Failed to purge records from the timeline index!", e );
        }
    }
}
