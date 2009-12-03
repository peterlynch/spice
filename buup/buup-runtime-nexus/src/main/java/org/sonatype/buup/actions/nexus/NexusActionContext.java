package org.sonatype.buup.actions.nexus;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.nexus.NexusBuup;

public class NexusActionContext
    extends HashMap<Object, Object>
    implements ActionContext
{
    private static final long serialVersionUID = 2444893663442298232L;

    private final NexusBuup nexusBuup;

    private final File basedir;

    private final File upgradeBundleBasedir;

    private final File upgradeBundleContentBasedir;

    private final File nexusAppDir;

    private final File nexusWorkDir;

    private final Map<String, String> parameters;

    public NexusActionContext( NexusBuup nexusBuup, File basedir, File upgradeBundleBasedir,
        File upgradeBundleContentBasedir, File nexusAppDir, File nexusWorkDir, Map<String, String> parameters )
    {
        super();

        this.nexusBuup = nexusBuup;
        this.basedir = basedir;
        this.upgradeBundleBasedir = upgradeBundleBasedir;
        this.upgradeBundleContentBasedir = upgradeBundleContentBasedir;
        this.nexusAppDir = nexusAppDir;
        this.nexusWorkDir = nexusWorkDir;
        this.parameters = Collections.unmodifiableMap( parameters );
    }

    public NexusBuup getBuup()
    {
        return nexusBuup;
    }

    public File getBasedir()
    {
        return basedir;
    }

    public File getUpgradeBundleBasedir()
    {
        return upgradeBundleBasedir;
    }

    public File getUpgradeBundleContentBasedir()
    {
        return upgradeBundleContentBasedir;
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public File getNexusAppDir()
    {
        return nexusAppDir;
    }

    public File getNexusWorkDir()
    {
        return nexusWorkDir;
    }

    public File getNexusSystemPluginRepositoryDir()
    {
        return new File( getNexusAppDir(), "plugin-repository" );
    }

    public File getNexusUserPluginRepositoryDir()
    {
        return new File( getNexusWorkDir(), "plugin-repository" );
    }
}
