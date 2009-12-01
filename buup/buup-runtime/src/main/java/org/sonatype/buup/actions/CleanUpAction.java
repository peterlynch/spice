package org.sonatype.buup.actions;

import org.codehaus.plexus.util.FileUtils;

public class CleanUpAction
    extends AbstractFileManipulatorAction
{
    public void perform( ActionContext ctx )
        throws Exception
    {
        // delete the exploded upgrade bundle from disk
        FileUtils.deleteDirectory( ctx.getBuup().getUpgradeBundleDirectory() );
    }
}
