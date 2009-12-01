package org.sonatype.buup.nexus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.buup.Buup;
import org.sonatype.buup.actions.Action;
import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.actions.CopyFilesToPlaceAction;
import org.sonatype.buup.actions.nexus.CheckNexusReadWritePermissionsAction;
import org.sonatype.buup.actions.nexus.NexusActionContext;
import org.sonatype.buup.actions.nexus.SetBundleMemoryAction;
import org.sonatype.buup.actions.nexus.ValidateNexusContextAction;
import org.sonatype.buup.cfgfiles.jsw.WrapperConfEditor;

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

    public File getNexusSystemPluginRepositoryDir()
    {
        return new File( getNexusAppDir(), "plugin-repository" );
    }

    public File getNexusUserPluginRepositoryDir()
    {
        return new File( getNexusWorkDir(), "plugin-repository" );
    }

    // == BUUP

    @Override
    protected void initialize()
        throws Exception
    {
        super.initialize();

        nexusAppDir = new File( getAppContext().getBasedir(), (String) getAppContext().get( "nexus-app" ) );

        nexusWorkDir = new File( getAppContext().getBasedir(), (String) getAppContext().get( "nexus-work" ) );
    }

    @Override
    public boolean doUpgrade()
        throws IOException
    {
        // copy the backup wrapper.conf (backed by invoker, since the actual one is already modified to run BUUP!) to
        // new place, get editor for it
        File wrapperConfToBeEdited =
            new File( getWrapperHelper().getBackupWrapperConfFile().getParentFile(), getWrapperHelper()
                .getBackupWrapperConfFile().getName()
                + ".work" );

        FileUtils.copyFile( getWrapperHelper().getBackupWrapperConfFile(), wrapperConfToBeEdited );

        WrapperConfEditor actionEditor = getWrapperHelper().getWrapperEditor( wrapperConfToBeEdited );

        // acreate actions to perform
        List<Action> actions = new ArrayList<Action>();

        // validate context
        actions.add( new ValidateNexusContextAction() );
        // check for proper FS perms in nexus-app and nexus-work too (not just basedir)
        actions.add( new CheckNexusReadWritePermissionsAction() );
        // set bundle memory if needed
        actions.add( new SetBundleMemoryAction() );
        // copy all JAR files from bundle to it's place
        actions.add( new CopyFilesToPlaceAction() );
        // etc.

        ActionContext ctx =
            new NexusActionContext( this, getAppContext().getBasedir(), new File( getUpgradeBundleDirectory(),
                "content" ), actionEditor );

        executeActions( ctx, actions );

        // save wrapper.conf potentially modified by actions
        actionEditor.save();

        // make the wrapper.conf the new wrapper.conf
        getWrapperHelper().swapInWrapperConf( wrapperConfToBeEdited );

        return true;
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
