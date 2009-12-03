package org.sonatype.buup.actions.nexus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.buup.actions.AbstractBatchDeleteObsoleteFilesAction;
import org.sonatype.buup.actions.ActionContext;

public class DeleteObsoleteAppFilesAction
    extends AbstractBatchDeleteObsoleteFilesAction
{
    @Override
    public void perform( ActionContext ctx )
        throws Exception
    {
        setTargetDir( ( (NexusActionContext) ctx ).getNexusAppDir() );

        super.perform( ctx );
    }

    @Override
    protected List<String> getFilePathsToDelete()
    {
        ArrayList<String> result = new ArrayList<String>();

        // scan in lib for nexus-oss-edition (this is mere a workaround to make it work while we do not release and have
        // timestmaped versions in too)
        File[] jars = getTargetDir().listFiles();

        if ( jars != null )
        {
            for ( File jar : jars )
            {
                if ( jar.getName().startsWith( "nexus-oss-edition" ) && jar.getName().endsWith( ".jar" )
                    && jar.getParentFile().getName().equals( "lib" ) )
                {
                    result.add( "lib/" + jar.getName() );
                }
            }
        }

        return result;
    }
}
