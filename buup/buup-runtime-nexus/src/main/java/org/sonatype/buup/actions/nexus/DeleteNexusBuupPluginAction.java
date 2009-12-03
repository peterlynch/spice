package org.sonatype.buup.actions.nexus;

import java.io.File;

import org.sonatype.buup.actions.AbstractFileManipulatorAction;
import org.sonatype.buup.actions.ActionContext;

public class DeleteNexusBuupPluginAction
    extends AbstractFileManipulatorAction
{
    public void perform( ActionContext ctx )
        throws Exception
    {
        File systemPluginRepository = ( (NexusActionContext) ctx ).getNexusSystemPluginRepositoryDir();

        File[] installedPlugins = systemPluginRepository.listFiles();

        if ( installedPlugins != null )
        {
            for ( File pluginDir : installedPlugins )
            {
                if ( pluginDir.isDirectory() && pluginDir.getName().startsWith( "nexus-buup-plugin" ) )
                {
                    deleteFile( ctx, pluginDir, true );
                }
            }
        }
    }
}
