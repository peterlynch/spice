package org.sonatype.buup.actions.nexus;

import java.io.File;

import org.sonatype.buup.actions.AbstractEditPropertiesAction;
import org.sonatype.buup.actions.ActionContext;

public abstract class AbstractEditNexusPlexusPropertiesAction
    extends AbstractEditPropertiesAction
{
    @Override
    public File getPropertiesFile( ActionContext ctx )
    {
        return resolveChildPath( ctx.getBasedir(), "conf/plexus.properties" );
    }
}
