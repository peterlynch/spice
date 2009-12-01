package org.sonatype.buup.actions.nexus;

import java.io.IOException;

import org.sonatype.buup.actions.AbstractFileManipulatorAction;
import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.cfgfiles.DefaultPropertiesFile;
import org.sonatype.buup.cfgfiles.PropertiesFile;

public abstract class AbstractEditNexusPlexusPropertiesAction
    extends AbstractFileManipulatorAction
{
    public void perform( ActionContext ctx )
        throws Exception
    {
        DefaultPropertiesFile plexusProperties =
            new DefaultPropertiesFile( resolveChildPath( ctx.getBasedir(), "conf/plexus.properties" ) );

        editPlexusProperties( plexusProperties );

        plexusProperties.save();
    }

    public abstract void editPlexusProperties( PropertiesFile file )
        throws IOException;
}
