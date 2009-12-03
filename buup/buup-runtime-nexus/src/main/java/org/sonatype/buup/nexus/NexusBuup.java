package org.sonatype.buup.nexus;

import java.io.File;

import org.sonatype.buup.Buup;
import org.sonatype.buup.actions.Action;
import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.actions.ActionList;
import org.sonatype.buup.actions.CleanUpAction;
import org.sonatype.buup.actions.CopyFilesToPlaceAction;
import org.sonatype.buup.actions.nexus.CheckNexusReadWritePermissionsAction;
import org.sonatype.buup.actions.nexus.DeleteNexusBuupPluginAction;
import org.sonatype.buup.actions.nexus.DeleteObsoleteAppFilesAction;
import org.sonatype.buup.actions.nexus.NexusActionContext;
import org.sonatype.buup.actions.nexus.NexusUpgradePlexusPropertiesAction;
import org.sonatype.buup.actions.nexus.SetBundleMemoryAction;
import org.sonatype.buup.actions.nexus.ValidateNexusContextAction;

public class NexusBuup
    extends Buup
{
    private File nexusAppDir;

    private File nexusWorkDir;

    public File getNexusAppDir()
    {
        return nexusAppDir;
    }

    public void setNexusAppDir( File nexusAppDir )
    {
        this.nexusAppDir = nexusAppDir;
    }

    public File getNexusWorkDir()
    {
        return nexusWorkDir;
    }

    public void setNexusWorkDir( File nexusWorkDir )
    {
        this.nexusWorkDir = nexusWorkDir;
    }

    // == BUUP

    @Override
    protected void initialize()
        throws Exception
    {
        super.initialize();

        nexusAppDir = new File( (String) getAppContext().get( "nexus-app" ) );

        nexusWorkDir = new File( (String) getAppContext().get( "nexus-work" ) );
    }

    @Override
    public ActionContext getActionContext()
    {
        return new NexusActionContext( this, getAppContext().getBasedir(), getUpgradeBundleDirectory(),
            getUpgradeBundleContentDirectory(), getNexusAppDir(), getNexusWorkDir(), getParameters() );
    }

    @Override
    public Action getAction()
    {
        // create actions to perform
        ActionList actions = new ActionList();

        // validate context
        actions.getActions().add( new ValidateNexusContextAction() );
        // check for proper FS perms in nexus-app and nexus-work too (not just basedir)
        actions.getActions().add( new CheckNexusReadWritePermissionsAction() );
        // copy all JAR files from bundle to it's place
        actions.getActions().add( new CopyFilesToPlaceAction() );
        // delete obsolete file
        actions.getActions().add( new DeleteObsoleteAppFilesAction() );
        // add p2 support to plexus.properties
        actions.getActions().add( new NexusUpgradePlexusPropertiesAction() );
        // set bundle memory if needed
        actions.getActions().add( new SetBundleMemoryAction() );

        // delete nexus-buup-plugin since it is not needed anymore
        actions.getActions().add( new DeleteNexusBuupPluginAction() );
        // clean up
        actions.getActions().add( new CleanUpAction() );
        // etc.

        return actions;
    }

    // == entry point

    /**
     * The entry point.
     */
    public static void main( String[] args )
    {
        new NexusBuup().upgrade();
    }

}
