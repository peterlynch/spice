package org.sonatype.buup.actions;

public class CleanUpAction
    extends AbstractFileManipulatorAction
{
    public void perform( ActionContext ctx )
        throws Exception
    {
        ctx.getBuup().getBackupManager().deleteFile( ctx.getUpgradeBundleBasedir() );
    }
}
