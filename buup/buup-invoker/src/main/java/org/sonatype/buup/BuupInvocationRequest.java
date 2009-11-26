package org.sonatype.buup;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BuupInvocationRequest
{
    private final File basedir;

    private final File upgradeBundleDirectory;

    private final Map<String, String> parameters;

    public BuupInvocationRequest( File basedir, File explodedUpgradeBundleDirectory, Map<String, String> parameters )
    {
        this.basedir = basedir;

        this.upgradeBundleDirectory = explodedUpgradeBundleDirectory;

        this.parameters = new HashMap<String, String>();

        if ( parameters != null )
        {
            this.parameters.putAll( parameters );
        }
    }

    public File getBasedir()
    {
        return basedir;
    }

    public File getUpgradeBundleDirectory()
    {
        return upgradeBundleDirectory;
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }
}
