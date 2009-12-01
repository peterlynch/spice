package org.sonatype.buup.actions.nexus;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.buup.actions.AbstractBatchDeleteObsoleteFilesAction;
import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.nexus.NexusBuup;

public class DeleteObsoleteAppFilesAction
    extends AbstractBatchDeleteObsoleteFilesAction
{
    @Override
    public void perform( ActionContext ctx )
        throws Exception
    {
        setTargetDir( ( (NexusBuup) ctx.getBuup() ).getNexusAppDir() );

        super.perform( ctx );
    }

    @Override
    protected List<String> getFilePathsToDelete()
    {
        ArrayList<String> result = new ArrayList<String>();

        result.add( "lib/nexus-oss-edition-1.4.1.jar" );
        // etc

        return result;
    }
}
