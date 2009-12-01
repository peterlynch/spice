package org.sonatype.buup.actions.nexus;

import java.io.File;
import java.util.HashMap;

import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.cfgfiles.jsw.WrapperConfEditor;
import org.sonatype.buup.nexus.NexusBuup;

public class NexusActionContext
    extends HashMap<String, Object>
    implements ActionContext
{
    private static final long serialVersionUID = 2444893663442298232L;

    private final NexusBuup nexusBuup;

    private final File basedir;

    private final File upgradeBundleBasedir;

    private final WrapperConfEditor wrapperConfEditor;

    public NexusActionContext( NexusBuup nexusBuup, File basedir, File upgradeBundleBasedir,
        WrapperConfEditor wrapperConfEditor )
    {
        super();

        this.nexusBuup = nexusBuup;
        this.basedir = basedir;
        this.upgradeBundleBasedir = upgradeBundleBasedir;
        this.wrapperConfEditor = wrapperConfEditor;
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

    public WrapperConfEditor getWrapperConfEditor()
    {
        return wrapperConfEditor;
    }
}
