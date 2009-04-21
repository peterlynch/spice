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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.component.annotations.Component;

@Component( role = TimelineIndexer.class )
public class DefaultTimelineIndexer
    implements TimelineIndexer
{
    private static final String TIMESTAMP = "_t";

    private static final String TYPE = "_1";

    private static final String SUBTYPE = "_2";

    private Directory directory;

    private IndexReader indexReader;

    private IndexWriter indexWriter;

    private IndexSearcher indexSearcher;

    public void configure( File indexDirectory )
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

                directory = FSDirectory.getDirectory( indexDirectory );

                if ( IndexReader.indexExists( directory ) )
                {
                    if ( IndexWriter.isLocked( directory ) )
                    {
                        IndexWriter.unlock( directory );
                    }

                    newIndex = false;
                }

                indexWriter = new IndexWriter(
                    indexDirectory,
                    new KeywordAnalyzer(),
                    newIndex,
                    IndexWriter.MaxFieldLength.LIMITED );

                closeIndexWriter();
            }
        }
        catch ( IOException e )
        {
            throw new TimelineException( "Fail to configure timeline index!", e );
        }

    }

    private IndexWriter getIndexWriter()
        throws IOException
    {
        if ( indexWriter == null )
        {
            indexWriter = new IndexWriter( directory, new KeywordAnalyzer(), false, IndexWriter.MaxFieldLength.LIMITED );
        }

        return indexWriter;
    }

    private void closeIndexWriter()
        throws IOException
    {
        if ( indexWriter != null )
        {
            indexWriter.commit();

            indexWriter.close();

            indexWriter = null;
        }
    }

    private IndexReader getIndexReader()
        throws IOException
    {
        if ( indexReader == null || !indexReader.isCurrent() )
        {
            if ( indexReader != null )
            {
                indexReader.close();
            }
            indexReader = IndexReader.open( directory );
        }
        return indexReader;
    }

    private IndexSearcher getIndexSearcher()
        throws IOException
    {
        if ( indexSearcher == null || getIndexReader() != indexSearcher.getIndexReader() )
        {
            if ( indexSearcher != null )
            {
                indexSearcher.close();

                // the reader was supplied explicitly
                indexSearcher.getIndexReader().close();
            }
            indexSearcher = new IndexSearcher( getIndexReader() );
        }
        return indexSearcher;
    }

    private void closeIndexReaderAndSearcher()
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

    private Document createDocument( TimelineRecord record )
    {
        Document doc = new Document();

        doc.add( new Field(
            TIMESTAMP,
            DateTools.timeToString( record.getTimestamp(), DateTools.Resolution.MINUTE ),
            Field.Store.NO,
            Field.Index.NOT_ANALYZED ) );

        doc.add( new Field( TYPE, record.getType(), Field.Store.NO, Field.Index.NOT_ANALYZED ) );

        doc.add( new Field( SUBTYPE, record.getSubType(), Field.Store.NO, Field.Index.NOT_ANALYZED ) );

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
            return new ConstantScoreRangeQuery(
                TIMESTAMP,
                DateTools.timeToString( from, DateTools.Resolution.MINUTE ),
                DateTools.timeToString( to, DateTools.Resolution.MINUTE ),
                true,
                true );
        }
        else
        {
            BooleanQuery result = new BooleanQuery();

            result.add(
                new ConstantScoreRangeQuery(
                    TIMESTAMP,
                    DateTools.timeToString( from, DateTools.Resolution.MINUTE ),
                    DateTools.timeToString( to, DateTools.Resolution.MINUTE ),
                    true,
                    true ),
                Occur.MUST );

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

    @SuppressWarnings( "unchecked" )
    public List<Map<String, String>> retrieve( long fromTime, long toTime, Set<String> types, Set<String> subTypes,
        int from, int count, TimelineFilter filter )
        throws TimelineException
    {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        try
        {
            synchronized ( this )
            {
                TopFieldDocs topDocs = getIndexSearcher().search(
                    buildQuery( fromTime, toTime, types, subTypes ),
                    null,
                    from + count,
                    new Sort( new SortField( TIMESTAMP, SortField.LONG, true ) ) );

                for ( int i = 0; i < topDocs.scoreDocs.length; i++ )
                {
                    // step over the unneeded stuff
                    if ( i < from )
                    {
                        continue;
                    }

                    ScoreDoc scoreDoc = topDocs.scoreDocs[i];

                    Document doc = getIndexSearcher().doc( scoreDoc.doc );

                    Map<String, String> map = new HashMap<String, String>();

                    for ( Field field : (List<Field>) doc.getFields() )
                    {
                        if ( !field.name().startsWith( "_" ) )
                        {
                            map.put( field.name(), field.stringValue() );
                        }
                    }

                    if ( filter != null && filter.accept( map ) )
                    {
                        result.add( map );
                    }
                    else if ( filter == null )
                    {
                        result.add( map );
                    }
                }

                closeIndexReaderAndSearcher();
            }
        }
        catch ( IOException e )
        {
            throw new TimelineException( "Failed to retrieve records from the timeline index!", e );
        }

        return result;
    }

    public int purge( long fromTime, long toTime, Set<String> types, Set<String> subTypes )
        throws TimelineException
    {
        try
        {
            synchronized ( this )
            {
                closeIndexWriter();

                IndexSearcher searcher = getIndexSearcher();

                if ( searcher.maxDoc() == 0 )
                {
                    return 0;
                }

                TopDocs topDocs = searcher.search( buildQuery( fromTime, toTime, types, subTypes ), searcher.maxDoc() );

                for ( ScoreDoc scoreDoc : topDocs.scoreDocs )
                {
                    searcher.getIndexReader().deleteDocument( scoreDoc.doc );
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
