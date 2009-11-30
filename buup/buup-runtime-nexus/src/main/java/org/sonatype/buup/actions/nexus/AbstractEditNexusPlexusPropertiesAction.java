package org.sonatype.buup.actions.nexus;

import java.io.IOException;

import org.sonatype.buup.actions.AbstractFileManipulatorAction;
import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.cfgfiles.DefaultPropertiesFile;
import org.sonatype.buup.cfgfiles.PropertiesFile;
import org.sonatype.buup.nexus.NexusBuup;

public abstract class AbstractEditNexusPlexusPropertiesAction
    extends AbstractFileManipulatorAction
{
    public AbstractEditNexusPlexusPropertiesAction( NexusBuup buup )
    {
        super( buup );
    }

    public void perform( ActionContext ctx )
        throws Exception
    {
        DefaultPropertiesFile plexusProperties =
            new DefaultPropertiesFile( resolveChildPath( getBuup().getAppContext().getBasedir(),
                "conf/plexus.properties" ) );

        editPlexusProperties( plexusProperties );

        plexusProperties.save();
    }

    public abstract void editPlexusProperties( PropertiesFile file )
        throws IOException;
}
