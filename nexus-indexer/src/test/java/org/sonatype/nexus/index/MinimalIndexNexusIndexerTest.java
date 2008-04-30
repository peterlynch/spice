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

import java.util.Collection;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;

/**
 * @author Jason van Zyl
 * @author Eugene Kuleshov
 */
public class MinimalIndexNexusIndexerTest
    extends AbstractRepoNexusIndexerTest
{
    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context = nexusIndexer.addIndexingContext(
            "test-minimal",
            "test",
            repo,
            indexDir,
            null,
            null, NexusIndexer.MINIMAL_INDEX );
        nexusIndexer.scan( context );
    }

    public void testPlugin()
        throws Exception
    {
        // String term = "plugin";
        // String term = "maven-core-it-plugin";
        String term = "org.apache.maven.plugins";

//        Query bq = new TermQuery(new Term(ArtifactInfo.GROUP_ID, "org.apache.maven.plugins"));
//        Query bq = new TermQuery(new Term(ArtifactInfo.ARTIFACT_ID, term));
        Query bq = new PrefixQuery( new Term( ArtifactInfo.GROUP_ID, term ) );
//        BooleanQuery bq = new BooleanQuery();
//        bq.add(new PrefixQuery(new Term(ArtifactInfo.GROUP_ID, term + "*")), Occur.SHOULD);
//        bq.add(new PrefixQuery(new Term(ArtifactInfo.ARTIFACT_ID, term + "*")), Occur.SHOULD);
        TermQuery tq = new TermQuery( new Term( ArtifactInfo.PACKAGING, "maven-plugin" ) );
        Query query = new FilteredQuery( tq, new QueryWrapperFilter( bq ) );

        Collection<ArtifactInfo> r = nexusIndexer.searchFlat( ArtifactInfo.VERSION_COMPARATOR, query );

        assertEquals( r.toString(), 1, r.size() );

        ArtifactInfo ai = r.iterator().next();

        assertEquals( "org.apache.maven.plugins", ai.groupId );
        assertEquals( "maven-core-it-plugin", ai.artifactId );
        assertEquals( "core-it", ai.prefix );

        List<String> goals = ai.goals;
        assertEquals( 14, goals.size() );
        assertEquals( "catch", goals.get( 0 ) );
        assertEquals( "fork", goals.get( 1 ) );
        assertEquals( "fork-goal", goals.get( 2 ) );
        assertEquals( "touch", goals.get( 3 ) );
        assertEquals( "setter-touch", goals.get( 4 ) );
        assertEquals( "generate-envar-properties", goals.get( 5 ) );
        assertEquals( "generate-properties", goals.get( 6 ) );
        assertEquals( "loadable", goals.get( 7 ) );
        assertEquals( "light-touch", goals.get( 8 ) );
        assertEquals( "package", goals.get( 9 ) );
        assertEquals( "reachable", goals.get( 10 ) );
        assertEquals( "runnable", goals.get( 11 ) );
        assertEquals( "throw", goals.get( 12 ) );
        assertEquals( "tricky-params", goals.get( 13 ) );
    }

}
