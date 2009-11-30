package org.sonatype.buup.nexus;

import java.io.File;

import org.sonatype.buup.Buup;

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
    {
        return false;
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
