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
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sonatype.nexus.index.context.IndexingContext;

/** http://issues.sonatype.org/browse/NEXUS-13 */
public class Nexus13NexusIndexerTest
    extends AbstractNexusIndexerTest
{
    protected File repo = new File( getBasedir(), "src/test/nexus-13" );

    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context = nexusIndexer.addIndexingContext(
            "nexus-13",
            "nexus-13",
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

        Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "cisco" );

        Map<String, ArtifactInfoGroup> r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( r.toString(), 4, r.size() ); // qdox and testng

        assertTrue( r.containsKey( "cisco.infra.dft : dma.plugin.utils" ) );
        assertTrue( r.containsKey( "cisco.infra.dft : dma.pom.enforcer" ) );
        assertTrue( r.containsKey( "cisco.infra.dft : maven-dma-mgmt-plugin" ) );
        assertTrue( r.containsKey( "cisco.infra.dft : maven-dma-plugin" ) );

        q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "*dma.plugin.utils" );

        r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( r.toString(), 1, r.size() );

        assertTrue( r.containsKey( "cisco.infra.dft : dma.plugin.utils" ) );
        assertEquals( "cisco.infra.dft : dma.plugin.utils", r.get( "cisco.infra.dft : dma.plugin.utils" ).getGroupKey() );
    }

    public void testSearchArchetypes()
        throws Exception
    {
        // TermQuery tq = new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-archetype"));
        // BooleanQuery bq = new BooleanQuery();
        // bq.add(new WildcardQuery(new Term(ArtifactInfo.GROUP_ID, term + "*")), Occur.SHOULD);
        // bq.add(new WildcardQuery(new Term(ArtifactInfo.ARTIFACT_ID, term + "*")), Occur.SHOULD);
        // FilteredQuery query = new FilteredQuery(tq, new QueryWrapperFilter(bq));

        Query query = new TermQuery( new Term( ArtifactInfo.PACKAGING, "maven-archetype" ) );

        Collection<ArtifactInfo> r = nexusIndexer.searchFlat( ArtifactInfo.VERSION_COMPARATOR, query );

        assertEquals( 1, r.size() );

        ArtifactInfo ai = r.iterator().next();
        assertEquals( "cisco.infra.dft", ai.groupId );
        assertEquals( "archetype.sdf", ai.artifactId );
        assertEquals( "1.0-SNAPSHOT", ai.version );
    }

    public void testIndexTimestamp()
        throws Exception
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        IndexUtils.packIndexArchive( context, os );

        Thread.sleep( 1000L );

        Directory newIndexDir = FSDirectory.getDirectory( new File( getBasedir(), "target/test-new" ) );

        IndexUtils.unpackIndexArchive( new ByteArrayInputStream( os.toByteArray() ), newIndexDir );

        IndexingContext newContext = nexusIndexer.addIndexingContext(
            "test-new",
            "nexus-13",
            null,
            newIndexDir,
            null,
            null,
            NexusIndexer.DEFAULT_INDEX );

        assertEquals( 0, newContext.getTimestamp().getTime() - context.getTimestamp().getTime() );

        assertEquals( context.getTimestamp(), newContext.getTimestamp() );

        // make sure context has the same artifacts

        Query query = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "cisco" );

        Collection<ArtifactInfo> r = nexusIndexer.searchFlat( query, newContext );

        assertEquals( 8, r.size() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( r );

        assertEquals( 8, list.size() );

        ArtifactInfo ai = list.get( 0 );

        assertEquals( "1.0-SNAPSHOT", ai.version );

        ai = list.get( 1 );

        assertEquals( "1.0-SNAPSHOT", ai.version );

        assertEquals( "nexus-13", ai.repository );
        
        newContext.close( true );
    }

    public void testRootGroups()
        throws Exception
    {
        Set<String> rootGroups = nexusIndexer.getRootGroups( context );
        assertEquals( rootGroups.toString(), 1, rootGroups.size() );

        assertGroup( 8, "cisco", context );
    }

    public void testSearchFlat()
        throws Exception
    {
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "cisco.infra" );

        Collection<ArtifactInfo> r = nexusIndexer.searchFlat( q );

        assertEquals( 8, r.size() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( r );

        assertEquals( 8, list.size() );

        ArtifactInfo ai = list.get( 0 );

        assertEquals( "1.0-SNAPSHOT", ai.version );

        ai = list.get( 1 );

        assertEquals( "nexus-13", ai.repository );

    }

    public void testSearchGrouped()
        throws Exception
    {
        // ----------------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------------
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "cisco.infra" );

        Map<String, ArtifactInfoGroup> r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( 8, r.size() );

        ArtifactInfoGroup ig = r.values().iterator().next();

        assertEquals( "cisco.infra.dft : archetype.sdf", ig.getGroupKey() );

        assertEquals( 1, ig.getArtifactInfos().size() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( ig.getArtifactInfos() );

        assertEquals( 1, list.size() );

        ArtifactInfo ai = list.get( 0 );

        assertEquals( "1.0-SNAPSHOT", ai.version );
    }

    public void testSearchGroupedProblematicNames()
        throws Exception
    {

        // ----------------------------------------------------------------------------
        // Artifacts with "problematic" names
        // ----------------------------------------------------------------------------

        // "-" in the name
        Query q = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "dma.integr*" );

        Map<String, ArtifactInfoGroup> r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( 1, r.size() );

        ArtifactInfoGroup ig = r.values().iterator().next();

        assertEquals( "cisco.infra.dft : dma.integration.tests", ig.getGroupKey() );

        assertEquals( 1, ig.getArtifactInfos().size() );
    }

    public void testIdentify()
        throws Exception
    {
        ArtifactInfo ai = nexusIndexer.identify( ArtifactInfo.SHA1, "c8a2ef9d92a4b857eae0f36c2e01481787c5cbf8" );

        assertNotNull( ai );

        assertEquals( "cisco.infra.dft", ai.groupId );

        assertEquals( "dma.plugin.utils", ai.artifactId );

        assertEquals( "1.0-SNAPSHOT", ai.version );

        // Using a file

        File artifact = new File( repo, "cisco/infra/dft/maven-dma-mgmt-plugin/1.0-SNAPSHOT/maven-dma-mgmt-plugin-1.0-20080409.021413-1.jar" );

        ai = nexusIndexer.identify( artifact );

        assertNotNull( ai );

        assertEquals( "cisco.infra.dft", ai.groupId );

        assertEquals( "maven-dma-mgmt-plugin", ai.artifactId );

        assertEquals( "1.0-SNAPSHOT", ai.version );
    }
}
