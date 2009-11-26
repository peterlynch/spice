package org.sonatype.buup.cfgfiles.jsw;

/**
 * A WrapperHelper configuration providing all the defaults for Sonatype Application Bundles, but still making those
 * overridable.
 * 
 * @author cstamas
 */
public class WrapperHelperConfiguration
{
    /**
     * The name of the wrapper.conf file.
     */
    private String wrapperConfName = "wrapper.conf";

    /**
     * The name of the backup of the wrapper.conf file.
     */
    private String wrapperConfBackupName = "wrapper.conf.bak";

    /**
     * The name of the conf dir.
     */
    private String confDirPath = "conf";

    public String getWrapperConfName()
    {
        return wrapperConfName;
    }

    public void setWrapperConfName( String wrapperConfName )
    {
        this.wrapperConfName = wrapperConfName;
    }

    public String getWrapperConfBackupName()
    {
        return wrapperConfBackupName;
    }

    public void setWrapperConfBackupName( String wrapperConfBackupName )
    {
        this.wrapperConfBackupName = wrapperConfBackupName;
    }

    public String getConfDirPath()
    {
        return confDirPath;
    }

    public void setConfDirPath( String confDirPath )
    {
        this.confDirPath = confDirPath;
    }
}
