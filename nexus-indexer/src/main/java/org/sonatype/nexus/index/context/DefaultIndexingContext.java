/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.index.context;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.IndexUtils;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;
import org.sonatype.nexus.index.creator.IndexCreator;

/**
 * The default nexus implementation.
 * 
 * @author Jason van Zyl
 * @author cstamas
 */
public class DefaultIndexingContext
    implements IndexingContext
{
    /**
     * A standard location for indices served up by a webserver.
     */
    private static final String INDEX_DIRECTORY = ".index";

    private static final String FLD_DESCRIPTOR = "DESCRIPTOR";

    private static final String FLD_DESCRIPTOR_CONTENTS = "NexusIndex";

    private static final String FLD_IDXINFO = "IDXINFO";

    private static final String VERSION = "1.0";

    private static final Term DESCRIPTOR_TERM = new Term( FLD_DESCRIPTOR, FLD_DESCRIPTOR_CONTENTS );

    private Directory indexDirectory;

    private File indexDirectoryFile;

    private String id;

    private String repositoryId;

    private File repository;

    private String repositoryUrl;

    private String indexUpdateUrl;

    private Analyzer analyzer;

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;

    private NexusIndexWriter indexWriter;

    private Date timestamp;

    private List<? extends IndexCreator> indexCreators;

    private DefaultIndexingContext( String id, String repositoryId, File repository, //
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexCreators )
    {
        this.id = id;

        this.repositoryId = repositoryId;

        this.repository = repository;

        this.repositoryUrl = repositoryUrl;

        this.indexUpdateUrl = indexUpdateUrl;

        this.analyzer = new NexusAnalyzer();

        this.indexReader = null;

        this.indexWriter = null;

        this.indexCreators = indexCreators;
    }

    public DefaultIndexingContext( String id, String repositoryId, File repository, File indexDirectoryFile,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexCreators )
        throws IOException,
            UnsupportedExistingLuceneIndexException
    {
        this( id, repositoryId, repository, repositoryUrl, indexUpdateUrl, indexCreators );

        this.indexDirectoryFile = indexDirectoryFile;

        this.indexDirectory = FSDirectory.getDirectory( indexDirectoryFile );

        prepareIndex();
    }

    public DefaultIndexingContext( String id, String repositoryId, File repository, Directory indexDirectory,
        String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexCreators )
        throws IOException,
            UnsupportedExistingLuceneIndexException
    {
        this( id, repositoryId, repository, repositoryUrl, indexUpdateUrl, indexCreators );

        this.indexDirectory = indexDirectory;

        if ( indexDirectory instanceof FSDirectory )
        {
            this.indexDirectoryFile = ( (FSDirectory) indexDirectory ).getFile();
        }

        prepareIndex();
    }

    public Directory getIndexDirectory()
    {
        return indexDirectory;
    }

    public File getIndexDirectoryFile()
    {
        return indexDirectoryFile;
    }

    private void prepareIndex()
        throws IOException,
            UnsupportedExistingLuceneIndexException
    {
        if ( IndexReader.indexExists( indexDirectory ) )
        {
            // unlock the dir forcibly
            if ( IndexReader.isLocked( indexDirectory ) )
            {
                IndexReader.unlock( indexDirectory );
            }

            checkAndUpdateIndexDescriptor();
        }
        else
        {
            if ( getRepositoryId() == null || getRepositoryId().length() == 0 )
            {
                throw new IllegalArgumentException( "The repositoryId cannot be null when creating new repository!" );
            }

            // create empty idx and store descriptor
            new NexusIndexWriter( indexDirectory, analyzer, true ).close();

            storeDescriptor();
        }

        timestamp = IndexUtils.getTimestamp( indexDirectory );

        if ( timestamp == null )
        {
            IndexUtils.updateTimestamp( indexDirectory, new Date( System.currentTimeMillis() ) );

            timestamp = IndexUtils.getTimestamp( indexDirectory );
        }
    }

    private void checkAndUpdateIndexDescriptor()
        throws IOException,
            UnsupportedExistingLuceneIndexException
    {
        Hits hits = getIndexSearcher().search( new TermQuery( DESCRIPTOR_TERM ) );

        if ( hits == null || hits.length() == 0 )
        {
            throw new UnsupportedExistingLuceneIndexException( "The existing index has no NexusIndexer descriptor" );
        }

        Document descriptor = hits.doc( 0 );

        if ( hits.length() != 1 )
        {
            storeDescriptor();
        }

        String[] h = StringUtils.split( descriptor.get( FLD_IDXINFO ), AbstractIndexCreator.FS );
        // String version = h[0];
        String repoId = h[1];

        // // compare version
        // if ( !VERSION.equals( version ) )
        // {
        // throw new UnsupportedExistingLuceneIndexException( "The existing index has version [" + version
        // + "] and not [" + VERSION + "] version!" );
        // }

        if ( getRepositoryId() == null )
        {
            repositoryId = repoId;

        }
        else if ( !getRepositoryId().equals( repoId ) )
        {
            throw new UnsupportedExistingLuceneIndexException( "The existing index is for repository " //
                + "[" + repoId + "] and not for repository [" + getRepositoryId() + "]" );
        }
    }

    private void storeDescriptor()
        throws IOException
    {
        IndexWriter w = getIndexWriter();
        try
        {
            w.deleteDocuments( DESCRIPTOR_TERM );

            Document hdr = new Document();

            hdr.add( new Field( FLD_DESCRIPTOR, FLD_DESCRIPTOR_CONTENTS, Field.Store.YES, Field.Index.UN_TOKENIZED ) );

            hdr.add( new Field(
                FLD_IDXINFO,
                VERSION + AbstractIndexCreator.FS + getRepositoryId(),
                Field.Store.YES,
                Field.Index.NO ) );
            w.addDocument( hdr );
        }
        finally
        {
            w.close();
        }
    }

    public String getId()
    {
        return id;
    }

    public void updateTimestamp()
        throws IOException
    {
        timestamp = new Date( System.currentTimeMillis() );
        // IndexUtils.updateTimestamp( indexDirectory, timestamp );
    }

    public Date getTimestamp()
    {
        return timestamp;
        // return IndexUtils.getTimestamp( indexDirectory );
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public File getRepository()
    {
        return repository;
    }

    public String getRepositoryUrl()
    {
        return repositoryUrl;
    }

    public String getIndexUpdateUrl()
    {
        if ( repositoryUrl != null )
        {
            if ( indexUpdateUrl == null || indexUpdateUrl.trim().length() == 0 )
            {
                return repositoryUrl + ( repositoryUrl.endsWith( "/" ) ? "" : "/" ) + INDEX_DIRECTORY;
            }
        }
        return indexUpdateUrl;
    }

    public Analyzer getAnalyzer()
    {
        return analyzer;
    }

    public IndexWriter getIndexWriter()
        throws IOException
    {
        if ( indexWriter == null || indexWriter.isClosed() )
        {
            indexWriter = new NexusIndexWriter( indexDirectory, analyzer, false );
        }
        return indexWriter;
    }

    public IndexReader getIndexReader()
        throws IOException
    {
        if ( indexReader == null || !indexReader.isCurrent() )
        {
            if ( indexReader != null )
            {
                indexReader.close();
            }
            indexReader = IndexReader.open( indexDirectory );
        }
        return indexReader;
    }

    public IndexSearcher getIndexSearcher()
        throws IOException
    {
        if ( indexSearcher == null || getIndexReader() != indexSearcher.getIndexReader() )
        {
            if ( indexSearcher != null )
            {
                indexSearcher.close();
            }
            indexSearcher = new IndexSearcher( getIndexReader() );
        }
        return indexSearcher;
    }

    public void close( boolean deleteFiles )
        throws IOException
    {
        IndexUtils.updateTimestamp( indexDirectory, timestamp );

        if ( indexDirectory != null )
        {
            if ( !deleteFiles )
            {
                getIndexWriter().optimize();
            }

            closeReaders();

            indexDirectory.close();
            if ( deleteFiles )
            {
                if ( indexDirectory instanceof FSDirectory )
                {
                    File file = ( (FSDirectory) indexDirectory ).getFile();
                    FileUtils.deleteDirectory( file );
                }
            }
        }
        // TODO: this will prevent from reopening them, but needs better solution
        indexDirectory = null;
    }

    private void closeReaders()
        throws CorruptIndexException,
            IOException
    {
        if ( indexWriter != null && !indexWriter.isClosed() )
        {
            indexWriter.close();
            indexWriter = null;
        }
        if ( indexSearcher != null )
        {
            indexSearcher.close();
            indexSearcher = null;
        }
        if ( indexReader != null )
        {
            indexReader.close();
            indexReader = null;
        }
    }

    // XXX need some locking for reader/writer
    public void replace( Directory directory )
        throws IOException
    {
        closeReaders();

        String[] names = indexDirectory.list();

        for ( int i = 0; i < names.length; i++ )
        {
            indexDirectory.deleteFile( names[i] );
        }

        Directory.copy( directory, indexDirectory, false );
    }

    public void merge( Directory directory )
        throws IOException
    {
        IndexWriter w = getIndexWriter();

        IndexSearcher s = getIndexSearcher();

        IndexReader r = IndexReader.open( directory );

        try
        {
            int numDocs = r.numDocs();
            for ( int i = 0; i < numDocs; i++ )
            {
                if ( r.isDeleted( i ) )
                {
                    continue;
                }

                Document d = r.document( i );
                String uinfo = d.get( ArtifactInfo.UINFO );

                if ( uinfo != null )
                {
                    Term term = new Term( ArtifactInfo.UINFO, uinfo );

                    Hits hits = s.search( new TermQuery( term ) );

                    if ( hits.length() == 0 )
                    {
                        ArtifactInfo info = constructArtifactInfo( null, d );
                        ArtifactContext artifactContext = new ArtifactContext( null, null, null, info );
                        ArtifactIndexingContext indexingContext = new DefaultArtifactIndexingContext( artifactContext );

                        Document doc = new Document();

                        doc.add( new Field( ArtifactInfo.UINFO, AbstractIndexCreator.getGAV(
                            info.groupId,
                            info.artifactId,
                            info.version,
                            info.classifier ), Field.Store.YES, Field.Index.UN_TOKENIZED ) );

                        for ( IndexCreator ic : getIndexCreators() )
                        {
                            ic.updateDocument( indexingContext, doc );
                        }

                        w.addDocument( doc );
                    }
                }
            }

            w.optimize();
            w.close();
        }
        finally
        {
            r.close();
        }
    }

    public List<IndexCreator> getIndexCreators()
    {
        return Collections.unmodifiableList( indexCreators );
    }

    public ArtifactInfo constructArtifactInfo( IndexingContext ctx, Document doc )
    {
        boolean res = false;

        ArtifactInfo artifactInfo = new ArtifactInfo();

        if ( ctx != null )
        {
            artifactInfo.context = ctx.getId();
        }

        for ( IndexCreator ic : getIndexCreators() )
        {
            res |= ic.updateArtifactInfo( this, doc, artifactInfo );
        }

        return res ? artifactInfo : null;
    }

}
