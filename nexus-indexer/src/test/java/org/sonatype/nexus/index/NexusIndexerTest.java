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
package org.sonatype.nexus.index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.IndexCreator;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;

/** @author Jason van Zyl */
public class NexusIndexerTest
    extends PlexusTestCase
{

    private IndexingContext context;

    public void testSearchGrouped()
        throws Exception
    {
        NexusIndexer nexus = prepare();

        // ----------------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------------
        Query q = nexus.constructQuery( ArtifactInfo.GROUP_ID, "qdox" );

        Map<String, ArtifactInfoGroup> r = nexus.searchGrouped( new GAGrouping(), q );

        assertEquals( 1, r.size() );

        ArtifactInfoGroup gi0 = r.values().iterator().next();

        assertEquals( "qdox : qdox", gi0.getGroupKey() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( gi0.getArtifactInfos() );

        ArtifactInfo ai0 = list.get( 0 );

        assertEquals( "1.6.1", ai0.version );

        ArtifactInfo ai1 = list.get( 1 );

        assertEquals( "1.5", ai1.version );

        assertEquals( "test", ai1.repository );

        // ----------------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------------

        // r = nexus.searchGrouped( new GAGrouping(), ArtifactInfo.INFO, "commons-log*" );
        r = nexus.searchGrouped( new GAGrouping(), String.CASE_INSENSITIVE_ORDER, new WildcardQuery( new Term(
            ArtifactInfo.UINFO,
            "commons-log*" ) ) );

        assertEquals( 1, r.size() );

        ArtifactInfoGroup gi1 = r.values().iterator().next();

        assertEquals( "commons-logging : commons-logging", gi1.getGroupKey() );
    }

    public void testSearchFlat()
        throws Exception
    {
        NexusIndexer nexus = prepare();

        Collection<ArtifactInfo> infos = nexus.searchFlat( ArtifactInfo.VERSION_COMPARATOR, new WildcardQuery(
            new Term( ArtifactInfo.UINFO, "*testng*" ) ) );

        assertEquals( 3, infos.size() );

        BooleanQuery bq = new BooleanQuery( true );
        bq.add( new WildcardQuery( new Term( ArtifactInfo.GROUP_ID, "testng*" ) ), Occur.SHOULD );
        bq.add( new WildcardQuery( new Term( ArtifactInfo.ARTIFACT_ID, "testng*" ) ), Occur.SHOULD );
        bq.setMinimumNumberShouldMatch( 1 );

        Collection<ArtifactInfo> infos1 = nexus.searchFlat( ArtifactInfo.VERSION_COMPARATOR, bq );

        assertEquals( 3, infos1.size() );
    }

    public void testSearchPackaging()
        throws Exception
    {
        NexusIndexer nexus = prepare();

        Collection<ArtifactInfo> infos = nexus.searchFlat( ArtifactInfo.VERSION_COMPARATOR, new WildcardQuery(
            new Term( ArtifactInfo.PACKAGING, "maven-plugin" ) ) );

        assertEquals( 1, infos.size() );
    }

    public void testIdentity()
        throws Exception
    {
        NexusIndexer nexus = prepare();

        // Search using SHA1 to find qdox 1.5

        ArtifactInfo ai = nexus.identify( ArtifactInfo.SHA1, "4d2db265eddf1576cb9d896abc90c7ba46b48d87" );

        assertNotNull( ai );

        assertEquals( "qdox", ai.groupId );

        assertEquals( "qdox", ai.artifactId );

        assertEquals( "1.5", ai.version );

        assertEquals( "test", ai.repository );

        // Using a file

        File artifact = new File( getBasedir(), "src/test/repo/qdox/qdox/1.5/qdox-1.5.jar" );

        ai = nexus.identify( artifact );

        assertNotNull( ai );

        assertEquals( "qdox", ai.groupId );

        assertEquals( "qdox", ai.artifactId );

        assertEquals( "1.5", ai.version );

        assertEquals( "test", ai.repository );
    }

    public void testUpdateArtifact()
        throws Exception
    {
        NexusIndexer nexus = prepare();

        Query query = new TermQuery( new Term(
            ArtifactInfo.UINFO,
            "org.apache.maven.plugins|maven-core-it-plugin|1.0|NA" ) );

        Collection<ArtifactInfo> res1 = nexus.searchFlat( ArtifactInfo.VERSION_COMPARATOR, query );

        assertEquals( 1, res1.size() );

        ArtifactInfo ai = res1.iterator().next();

        assertEquals( "Maven Core Integration Test Plugin", ai.name );

        long oldSize = ai.size;

        ai.name = "bla bla bla";

        ai.size += 100;

        IndexingContext indexingContext = nexus.getIndexingContexts().get( "test" );

        // String fname = indexingContext.getRepository().getAbsolutePath() + "/" + ai.groupId.replace( '.', '/' ) + "/"
        //     + ai.artifactId + "/" + ai.version + "/" + ai.artifactId + "-" + ai.version;

        // File pom = new File( fname + ".pom" );

        // File artifact = new File( fname + ".jar" );

        nexus.addArtifactToIndex( new ArtifactContext( null, null, null, ai ), indexingContext );

        Collection<ArtifactInfo> res2 = nexus.searchFlat( ArtifactInfo.VERSION_COMPARATOR, query );

        assertEquals( 1, res2.size() );

        ArtifactInfo ai2 = res2.iterator().next();

        assertEquals( oldSize + 100, ai2.size );

        assertEquals( "bla bla bla", ai2.name );
    }

    public void testUnpack()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        String indexId = context.getId();
        String repositoryId = context.getRepositoryId();
        File repository = context.getRepository();
        String repositoryUrl = context.getRepositoryUrl();
        List<IndexCreator> indexCreators = context.getIndexCreators();
        Directory directory = context.getIndexDirectory();

        RAMDirectory newDirectory = new RAMDirectory();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        IndexUtils.packIndexArchive( context, bos );

        IndexUtils.unpackIndexArchive( new ByteArrayInputStream( bos.toByteArray() ), newDirectory );

        indexer.removeIndexingContext( context, false );

        indexer
            .addIndexingContext( indexId, repositoryId, repository, newDirectory, repositoryUrl, null, indexCreators );

        Collection<ArtifactInfo> infos = indexer.searchFlat( ArtifactInfo.VERSION_COMPARATOR, new WildcardQuery(
            new Term( ArtifactInfo.PACKAGING, "maven-plugin" ) ) );

        assertEquals( 1, infos.size() );
    }

    private NexusIndexer prepare()
        throws Exception,
            IOException,
            UnsupportedExistingLuceneIndexException
    {
        NexusIndexer indexer = (NexusIndexer) lookup( NexusIndexer.class );

        Directory indexDir = new RAMDirectory();
        // File indexDir = new File( getBasedir(), "target/index/test-" + Long.toString( System.currentTimeMillis() ) );
        // FileUtils.deleteDirectory( indexDir );

        File repo = new File( getBasedir(), "src/test/repo" );

        List<IndexCreator> indexCreators = new ArrayList<IndexCreator>( 1 );
        indexCreators.add( new MinimalArtifactInfoIndexCreator() );
        context = indexer.addIndexingContext( "test", "test", repo, indexDir, null, null, indexCreators );
        indexer.scan( context );

//        IndexReader indexReader = context.getIndexSearcher().getIndexReader();
//        int numDocs = indexReader.numDocs();
//        for ( int i = 0; i < numDocs; i++ ) 
//        {
//            Document doc = indexReader.document( i );
//            System.err.println( i + " : " + doc.get( ArtifactInfo.UINFO));
//          
//        }
        return indexer;
    }

//    private void printDocs(NexusIndexer nexus) throws IOException 
//    {
//        IndexingContext context = nexus.getIndexingContexts().get("test");
//        IndexReader reader = context.getIndexSearcher().getIndexReader();
//        int numDocs = reader.numDocs();
//        for (int i = 0; i < numDocs; i++) {
//          Document doc = reader.document(i);  
//          System.err.println(i + " " + doc.get(ArtifactInfo.UINFO) + " : " + doc.get(ArtifactInfo.PACKAGING));
//        }
//    }
}
