package org.sonatype.buup.cfgfiles.jsw;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.buup.cfgfiles.DefaultPropertiesFile;
import org.sonatype.buup.cfgfiles.PropertiesFile;

/**
 * This is a JSW Wrapper helper class to manipulate wrapper.conf. It has only one "dependency": to have set the
 * "basedir" property on System properties with path pointing to the root of the Application bundle. This is the case
 * with Nexus bundle.
 * 
 * @author cstamas
 */
public class WrapperHelper
{
    /**
     * The configuration to use. It is lazily instantiated, see getConfiguration() method.
     */
    private WrapperHelperConfiguration wrapperHelperConfiguration;

    /**
     * The basedir from which to use this helper.
     */
    private final File basedir;

    public WrapperHelper( File basedir )
    {
        this( basedir, null );
    }

    public WrapperHelper( File basedir, WrapperHelperConfiguration config )
    {
        this.basedir = basedir;

        this.wrapperHelperConfiguration = config;
    }

    /**
     * Gets the configuration in use.
     * 
     * @return
     */
    public WrapperHelperConfiguration getConfiguration()
    {
        if ( wrapperHelperConfiguration == null )
        {
            wrapperHelperConfiguration = new WrapperHelperConfiguration();
        }

        return wrapperHelperConfiguration;
    }

    /**
     * Sets the configuration in use.
     * 
     * @param cfg
     */
    public void setConfiguration( WrapperHelperConfiguration cfg )
    {
        wrapperHelperConfiguration = cfg;
    }

    /**
     * Backups wrapper.conf if not backed up already.
     * 
     * @throws IOException
     */
    public boolean backupWrapperConf()
        throws IOException
    {
        return backupWrapperConf( false );
    }

    /**
     * Backups wrapper.conf. It may be forced to do so, potentionally overwriting the backup file.
     * 
     * @param overwrite true, if you want to overwrite the backup file even if it exists.
     * @throws IOException
     */
    public boolean backupWrapperConf( boolean overwrite )
        throws IOException
    {
        File wrapperConfBackup = getBackupWrapperConfFile();

        if ( overwrite || !wrapperConfBackup.isFile() )
        {
            FileUtils.copyFile( getWrapperConfFile(), wrapperConfBackup );

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Restores the conf file from backup
     * 
     * @throws IOException
     */
    public void restoreWrapperConf()
        throws IOException
    {
        FileUtils.copyFile( getBackupWrapperConfFile(), getWrapperConfFile() );
    }

    /**
     * Replaces the wrapper.conf file with the provided one.
     * 
     * @param file
     * @throws IOException
     */
    public void swapInWrapperConf( File file )
        throws IOException
    {
        FileUtils.copyFile( file, getWrapperConfFile() );
    }

    /**
     * Replaces the wrapper.conf file with the provided one. Nice to have if configuration file comes from classpath.
     * 
     * @param url
     * @throws IOException
     */
    public void swapInWrapperConf( URL url )
        throws IOException
    {
        FileUtils.copyURLToFile( url, getWrapperConfFile() );
    }

    /**
     * Return the File that points to wrapper.conf.
     * 
     * @return
     */
    public File getWrapperConfFile()
    {
        return getConfFile( getConfiguration().getWrapperConfName() );
    }

    /**
     * Returns a File that points to the backup wrapper.conf. This file <b>may not exist</b>, so check is needed.
     * 
     * @return
     */
    public File getBackupWrapperConfFile()
    {
        return getConfFile( getConfiguration().getWrapperConfBackupName() );
    }

    /**
     * Returns any configuration file from /conf directory of bundle.
     * 
     * @param name for example "wrapper.conf", but better use getWrapperConfFile() method for that!
     * @return
     */
    public File getConfFile( String name )
    {
        return new File( getConfDir(), name );
    }

    /**
     * Returns the File pointing to /conf directory of the bundle.
     * 
     * @return
     */
    public File getConfDir()
    {
        return new File( basedir, getConfiguration().getConfDirPath() );
    }

    /**
     * Returns WrapperConfWrapper for the wrapper.conf of the bundle. It may be used with WrapperEditor for some
     * high-level editing.
     * 
     * @return
     * @throws IOException if the file that is to be load broken, not found, etc.
     */
    public PropertiesFile getWrapperConfWrapper()
        throws IOException
    {
        return getWrapperConfWrapper( getWrapperConfFile() );
    }

    /**
     * Returns WrapperConfWrapper for the backed-up wrapper.conf of the bundle. It may be used with WrapperEditor for
     * some high-level editing.
     * 
     * @return
     * @throws IOException if the file that is to be load broken, not found, etc.
     */
    public PropertiesFile getBackupWrapperConfWrapper()
        throws IOException
    {
        return getWrapperConfWrapper( getBackupWrapperConfFile() );
    }

    /**
     * Returns WrapperConfWrapper for the provided file. It may be used with WrapperEditor for some high-level editing.
     * 
     * @return
     * @throws IOException if the file that is to be load broken, not found, etc.
     */
    public PropertiesFile getWrapperConfWrapper( File fileToWrap )
        throws IOException
    {
        return new DefaultPropertiesFile( fileToWrap );
    }

    /**
     * Returns WrapperConfEditor for the wrapper.conf of the bundle. It may be used with WrapperEditor for some
     * high-level editing.
     * 
     * @return
     * @throws IOException if the file that is to be load broken, not found, etc.
     */
    public WrapperConfEditor getWrapperConfEditor()
        throws IOException
    {
        return getWrapperEditor( getWrapperConfFile() );
    }

    /**
     * Returns WrapperConfEditor for the backed-up wrapper.conf of the bundle. It may be used with WrapperEditor for
     * some high-level editing.
     * 
     * @return
     * @throws IOException if the file that is to be load broken, not found, etc.
     */
    public WrapperConfEditor getBackupWrapperConfEditor()
        throws IOException
    {
        return getWrapperEditor( getBackupWrapperConfFile() );
    }

    /**
     * Returns WrapperConfEditor for the provided file. It may be used with WrapperEditor for some high-level editing.
     * 
     * @return
     * @throws IOException if the file that is to be load broken, not found, etc.
     */
    public WrapperConfEditor getWrapperEditor( File fileToWrap )
        throws IOException
    {
        return new DefaultWrapperConfEditor( getWrapperConfWrapper( fileToWrap ) );
    }

    // == private stuff

}
