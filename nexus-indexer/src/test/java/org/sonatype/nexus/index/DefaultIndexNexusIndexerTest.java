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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sonatype.nexus.index.context.IndexingContext;

/** @author Jason van Zyl */
public class DefaultIndexNexusIndexerTest
    extends AbstractRepoNexusIndexerTest
{

    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context = nexusIndexer.addIndexingContext(
            "test-default",
            "test",
            repo,
            indexDir,
            null,
            null,
            NexusIndexer.DEFAULT_INDEX );
        nexusIndexer.scan( context );
    }

    public void testSearchGroupedClasses()
        throws Exception
    {
        // ----------------------------------------------------------------------------
        // Classes and packages
        // ----------------------------------------------------------------------------

        Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "com/thoughtworks/qdox" );

        Map<String, ArtifactInfoGroup> r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( r.toString(), 2, r.size() ); // qdox and testng

        assertTrue( r.containsKey( "qdox : qdox" ) );
        assertTrue( r.containsKey( "org.testng : testng" ) );
        assertEquals( "qdox : qdox", r.get( "qdox : qdox" ).getGroupKey() );
        assertEquals( "org.testng : testng", r.get( "org.testng : testng" ).getGroupKey() );

        q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "com.thoughtworks.qdox" );

        r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( r.toString(), 2, r.size() );

        assertTrue( r.containsKey( "qdox : qdox" ) );
        assertTrue( r.containsKey( "org.testng : testng" ) );
        assertEquals( "qdox : qdox", r.get( "qdox : qdox" ).getGroupKey() );
        assertEquals( "org.testng : testng", r.get( "org.testng : testng" ).getGroupKey() );

        q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "thoughtworks" );

        r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( r.toString(), 2, r.size() );

        assertTrue( r.containsKey( "qdox : qdox" ) );
        assertTrue( r.containsKey( "org.testng : testng" ) );
        assertEquals( "qdox : qdox", r.get( "qdox : qdox" ).getGroupKey() );
        assertEquals( "org.testng : testng", r.get( "org.testng : testng" ).getGroupKey() );

        q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "Logger" );

        r = nexusIndexer.searchGrouped( new GGrouping(), q );

        assertEquals( r.toString(), 1, r.size() );

        ArtifactInfoGroup ig = r.values().iterator().next();

        assertEquals( r.toString(), "org.slf4j", ig.getGroupKey() );

        q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "*slf4j*Logg*" );

        r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( r.toString(), 2, r.size() );

        ig = r.values().iterator().next();

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( ig.getArtifactInfos() );

        assertEquals( r.toString(), 2, list.size() );

        ArtifactInfo ai = list.get( 0 );

        assertEquals( "org.slf4j", ai.groupId );

        assertEquals( "slf4j-api", ai.artifactId );

        assertEquals( "1.4.2", ai.version );

        ai = list.get( 1 );

        assertEquals( "org.slf4j", ai.groupId );

        assertEquals( "slf4j-api", ai.artifactId );

        assertEquals( "1.4.1", ai.version );

        // This was error, since slf4j-log4j12 DOES NOT HAVE any class for this search!
        ig = r.get( "org.slf4j : slf4j-log4j12" );

        list = new ArrayList<ArtifactInfo>( ig.getArtifactInfos() );

        assertEquals( list.toString(), 1, list.size() );

        ai = list.get( 0 );

        assertEquals( "org.slf4j", ai.groupId );

        assertEquals( "slf4j-log4j12", ai.artifactId );

        assertEquals( "1.4.1", ai.version );
    }
    
    public void testSearchArchetypes()
        throws Exception
    {
//      TermQuery tq = new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-archetype"));
//      BooleanQuery bq = new BooleanQuery();
//      bq.add(new WildcardQuery(new Term(ArtifactInfo.GROUP_ID, term + "*")), Occur.SHOULD);
//      bq.add(new WildcardQuery(new Term(ArtifactInfo.ARTIFACT_ID, term + "*")), Occur.SHOULD);
//      FilteredQuery query = new FilteredQuery(tq, new QueryWrapperFilter(bq));

      Query query = new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-archetype"));

      Collection<ArtifactInfo> r = nexusIndexer.searchFlat( ArtifactInfo.VERSION_COMPARATOR, query );
      
      assertEquals( 2, r.size() );
      
      Iterator<ArtifactInfo> it = r.iterator();
      {
          ArtifactInfo ai = it.next();
          assertEquals( "org.apache.directory.server", ai.groupId );
          assertEquals( "apacheds-schema-archetype", ai.artifactId );
          assertEquals( "1.0.2", ai.version );
      }
      {
          ArtifactInfo ai = it.next();
          assertEquals( "org.terracotta.maven.archetypes", ai.groupId );
          assertEquals( "pojo-archetype", ai.artifactId );
          assertEquals( "1.0.3", ai.version );
      }
      
    }
    
    public void testIndexTimestamp() throws Exception 
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        IndexUtils.packIndexArchive( context, os );
        
        Thread.sleep( 1000L );
        
        Directory newIndexDir = FSDirectory.getDirectory( new File( getBasedir(), "target/test-new" ) );
        
        IndexUtils.unpackIndexArchive( new ByteArrayInputStream( os.toByteArray() ), newIndexDir );
        
        IndexingContext newContext = nexusIndexer.addIndexingContext( 
            "test-new", 
            "test", 
            null, 
            newIndexDir, 
            null, 
            null, 
            NexusIndexer.DEFAULT_INDEX );
        
        assertEquals( 0, newContext.getTimestamp().getTime() - context.getTimestamp().getTime() );

        assertEquals( context.getTimestamp(), newContext.getTimestamp() );

        // make sure context has the same artifacts
        
        Query query = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "qdox" );
        
        Collection<ArtifactInfo> r = nexusIndexer.searchFlat( query, newContext );

        assertEquals( 2, r.size() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( r );

        assertEquals( 2, list.size() );

        ArtifactInfo ai = list.get( 0 );

        assertEquals( "1.6.1", ai.version );

        ai = list.get( 1 );

        assertEquals( "1.5", ai.version );

        assertEquals( "test", ai.repository );
        
        newContext.close( true );
    }

}
