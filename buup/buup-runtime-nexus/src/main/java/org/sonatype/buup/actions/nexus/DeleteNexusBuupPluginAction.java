package org.sonatype.buup.actions.nexus;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.buup.actions.AbstractFileManipulatorAction;
import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.nexus.NexusBuup;

public class DeleteNexusBuupPluginAction
    extends AbstractFileManipulatorAction
{
    public void perform( ActionContext ctx )
        throws Exception
    {
        File systemPluginRepository = ( (NexusBuup) ctx.getBuup() ).getNexusSystemPluginRepositoryDir();

        File[] installedPlugins = systemPluginRepository.listFiles();

        if ( installedPlugins != null )
        {
            for ( File pluginDir : installedPlugins )
            {
                if ( pluginDir.getName().startsWith( "nexus-buup-plugin" ) )
                {
                    FileUtils.deleteDirectory( pluginDir );

                    return;
                }
            }
        }
    }
}
