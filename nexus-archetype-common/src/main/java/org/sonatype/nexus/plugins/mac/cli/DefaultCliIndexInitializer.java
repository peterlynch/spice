package org.sonatype.nexus.plugins.mac.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.updater.IndexUpdateRequest;
import org.sonatype.nexus.index.updater.IndexUpdater;
import org.sonatype.nexus.index.updater.ResourceFetcher;

@Component( role = CliIndexInitializer.class )
public class DefaultCliIndexInitializer
    implements CliIndexInitializer
{
    @Requirement
    private NexusIndexer indexer;

    @Requirement
    private IndexUpdater indexUpdater;

    @Requirement( role = IndexCreator.class )
    private List<IndexCreator> indexCreators;

    private IndexingContext context;

    public IndexingContext initializeIndex( File indexDir, File tmpDir )
    {
        try
        {
            File tmpRepoDir = new File( tmpDir, "tmpRepo" );

            File tmpIndexDir = new File( tmpDir, "tmpIndex" );

            if ( tmpRepoDir.exists() )
            {
                FileUtils.deleteDirectory( tmpRepoDir );
            }

            if ( tmpIndexDir.exists() )
            {
                FileUtils.deleteDirectory( tmpIndexDir );
            }

            tmpRepoDir.mkdir();
            tmpIndexDir.mkdir();

            context = indexer.addIndexingContext( "tmp", "tmp", tmpRepoDir, tmpIndexDir, null, null, indexCreators );

            indexer.scan( context );

            IndexUpdateRequest updateRequest = new IndexUpdateRequest( context );

            updateRequest.setResourceFetcher( new FileFetcher( indexDir ) );

            updateRequest.setForceFullUpdate( true );

            indexUpdater.fetchAndUpdateIndex( updateRequest );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        catch ( UnsupportedExistingLuceneIndexException e )
        {
            e.printStackTrace();
        }

        return context;
    }

    public static class FileFetcher
        implements ResourceFetcher
    {

        private final File basedir;

        public FileFetcher( File basedir )
        {
            this.basedir = basedir;
        }

        public void connect( String id, String url )
            throws IOException
        {
            // don't need to do anything
        }

        public void disconnect()
            throws IOException
        {
            // don't need to do anything
        }

        public void retrieve( String name, File targetFile )
            throws IOException,
                FileNotFoundException
        {
            FileUtils.copyFile( getFile( name ), targetFile );

        }

        public InputStream retrieve( String name )
            throws IOException,
                FileNotFoundException
        {
            return new FileInputStream( getFile( name ) );
        }

        private File getFile( String name )
        {
            return new File( basedir, name );
        }

    }

}
