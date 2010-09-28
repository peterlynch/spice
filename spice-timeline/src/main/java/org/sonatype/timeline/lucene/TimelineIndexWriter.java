package org.sonatype.timeline.lucene;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

public class TimelineIndexWriter
    extends IndexWriter
{
    public TimelineIndexWriter( Directory d, Analyzer a, boolean create, MaxFieldLength mfl )
        throws CorruptIndexException, LockObtainFailedException, IOException
    {
        super( d, a, create, mfl );
    }

    public boolean hasUncommitedChanges()
    {
        try
        {
            Field pendingCommit = IndexWriter.class.getDeclaredField( "pendingCommit" );

            pendingCommit.setAccessible( true );

            return pendingCommit.get( this ) != null;
        }
        catch ( Exception x )
        {
            if ( x instanceof RuntimeException )
            {
                throw (RuntimeException) x;
            }
            else
            {
                throw new RuntimeException( "Could not access the \"IndexWriter.pendingCommit\" field!", x );
            }
        }
    }
}
