package org.sonatype.buup.actions.nexus;

import java.io.IOException;

import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.actions.CheckReadWritePermissionsAction;

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
        checkRWAccess( ( (NexusActionContext) ctx ).getNexusAppDir() );

        // and also nexus work dir
        checkRWAccess( ( (NexusActionContext) ctx ).getNexusWorkDir() );
    }
}
