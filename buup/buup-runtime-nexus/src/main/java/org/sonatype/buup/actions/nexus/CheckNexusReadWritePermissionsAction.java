package org.sonatype.buup.actions.nexus;

import java.io.IOException;

import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.actions.CheckReadWritePermissionsAction;
import org.sonatype.buup.nexus.NexusBuup;

public class CheckNexusReadWritePermissionsAction
    extends CheckReadWritePermissionsAction
{
    @Override
    public void perform( ActionContext ctx )
        throws IOException
    {
        // check basedir
        super.perform( ctx );

        // but also nexus app dir
        checkRWAccess( ( (NexusBuup) ctx.getBuup() ).getNexusAppDir() );

        // and also nexus work dir
        checkRWAccess( ( (NexusBuup) ctx.getBuup() ).getNexusWorkDir() );
    }
}
