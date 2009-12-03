package org.sonatype.buup.actions;

import java.io.IOException;

public class FailingAction
    extends AbstractAction
{
    public void perform( ActionContext ctx )
        throws Exception
    {
        throw new IOException( "Action " + getClass().getName() + " always fail (use for testing purposes!)" );
    }
}
