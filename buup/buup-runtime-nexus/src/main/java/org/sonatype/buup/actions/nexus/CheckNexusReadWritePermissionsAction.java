package org.sonatype.buup.actions.nexus;

import java.io.IOException;

import org.sonatype.buup.Buup;
import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.actions.CheckReadWritePermissionsAction;
import org.sonatype.buup.nexus.NexusBuup;

public class CheckNexusReadWritePermissionsAction
    extends CheckReadWritePermissionsAction
{
    public CheckNexusReadWritePermissionsAction( Buup buup )
    {
        super( buup );
    }

    @Override
    public void perform( ActionContext ctx )
        throws IOException
    {
        checkRWAccess( ( (NexusBuup) getBuup() ).getNexusAppDir() );

        checkRWAccess( ( (NexusBuup) getBuup() ).getNexusWorkDir() );
    }
}
