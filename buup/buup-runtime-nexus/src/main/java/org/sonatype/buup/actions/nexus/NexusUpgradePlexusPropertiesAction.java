package org.sonatype.buup.actions.nexus;

import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.cfgfiles.PropertiesFile;

public class NexusUpgradePlexusPropertiesAction
    extends AbstractEditNexusPlexusPropertiesAction
{
    @Override
    public void editPlexusProperties( ActionContext ctx, PropertiesFile file )
    {
        // set runtime-tmp
        file.setProperty( "runtime-tmp", "${runtime}/tmp" );
        // pointer to p2 runtime
        file.setProperty( "equinox-runtimeLocation", "${basedir}/p2/eclipse" );
    }
}
