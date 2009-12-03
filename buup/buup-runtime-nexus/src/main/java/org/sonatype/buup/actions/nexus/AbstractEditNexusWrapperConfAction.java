package org.sonatype.buup.actions.nexus;

import java.io.File;

import org.sonatype.buup.actions.AbstractEditWrapperConfAction;
import org.sonatype.buup.actions.ActionContext;

public abstract class AbstractEditNexusWrapperConfAction
    extends AbstractEditWrapperConfAction
{
    @Override
    public File getWrapperConfFile( ActionContext ctx )
    {
        return resolveChildPath( ctx.getBasedir(), "conf/wrapper.conf" );
    }
}
