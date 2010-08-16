package org.apache.lucene.index;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * TODO: this same class if also found in nexus-indexer 3.0.4+. This should be moved to some shared artifact like
 * lucene-core-ext or sonatype.
 * 
 * @author cstamas
 */
public class ExtendedIndexWriter
    extends IndexWriter
{
    @Deprecated
    public ExtendedIndexWriter( Directory d, boolean autoCommit, Analyzer a, boolean create )
        throws CorruptIndexException, LockObtainFailedException, IOException
    {
        super( d, autoCommit, a, create );
    }

    public ExtendedIndexWriter( Directory d, Analyzer a, boolean create, MaxFieldLength mfl )
        throws CorruptIndexException, LockObtainFailedException, IOException
    {
        super( d, a, create, mfl );
    }

    public boolean hasUncommitedChanges()
    {
        return pendingCommit != null;
    }
}
