package org.sonatype.buup.nexus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.buup.Buup;
import org.sonatype.buup.actions.Action;
import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.actions.nexus.CheckNexusReadWritePermissionsAction;
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

        nexusAppDir = new File( getAppContext().getBasedir(), (String) getAppContext().get( "nexus-app" ) );

        nexusWorkDir = new File( getAppContext().getBasedir(), (String) getAppContext().get( "nexus-work" ) );
    }

    @Override
    public boolean doUpgrade()
        throws IOException
    {
        // acreate actions to perform
        List<Action> actions = new ArrayList<Action>();

        actions.add( new ValidateNexusContextAction() );
        actions.add( new CheckNexusReadWritePermissionsAction() );
        // etc.

        ActionContext ctx = new ActionContext();

        executeActions( ctx, actions );

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
