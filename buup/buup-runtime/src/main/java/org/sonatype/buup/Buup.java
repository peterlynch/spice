package org.sonatype.buup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextFactory;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.PropertiesFileContextFiller;
import org.sonatype.buup.actions.Action;
import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.backup.BackupManager;
import org.sonatype.buup.backup.DefaultBackupManager;
import org.sonatype.buup.cfgfiles.jsw.WrapperHelper;

/**
 * This is the main entry class of the BUUP.
 * 
 * @author cstamas
 */
public class Buup
{
    private Logger logger = LoggerFactory.getLogger( getClass() );

    public Logger getLogger()
    {
        return logger;
    }

    private AppContext appContext;

    private File upgradeBundleDirectory;

    private Map<String, String> parameters;

    private BackupManager backupManager;

    private WrapperHelper wrapperHelper;

    public AppContext getAppContext()
    {
        return appContext;
    }

    public File getUpgradeBundleDirectory()
    {
        return upgradeBundleDirectory;
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public BackupManager getBackupManager()
    {
        return backupManager;
    }

    public WrapperHelper getWrapperHelper()
    {
        return wrapperHelper;
    }

    public boolean upgrade()
    {
        try
        {
            initialize();

            return performUpgrade();
        }
        catch ( Throwable t )
        {
            getLogger().error( "Upgrade failed.", t );

            return false;
        }
    }

    protected void initialize()
        throws Exception
    {
        // create app context to have access to same values as bundle app has, with same transformations and
        // overrides
        appContext = createAppContext();

        upgradeBundleDirectory = initUpgradeBundleDirectory();

        parameters = initInvokerParameters();

        backupManager = new DefaultBackupManager( this );

        wrapperHelper = new WrapperHelper( appContext.getBasedir() );
    }

    /**
     * We have to create the same appContext that Nexus bundle does.
     * 
     * @return
     * @throws AppContextException
     */
    public AppContext createAppContext()
        throws AppContextException
    {
        AppContextFactory ctxFactory = new AppContextFactory();

        AppContextRequest request = ctxFactory.getDefaultAppContextRequest();

        request.setName( "plexus" );

        // create a properties filler for plexus.properties, that will fail if props file not found
        File containerPropertiesFile;

        String plexusCfg = System.getProperty( "plexus.container.properties.file" );

        if ( plexusCfg != null )
        {
            containerPropertiesFile = new File( plexusCfg );
        }
        else
        {
            // set relative file, and will be resolved against basedir
            containerPropertiesFile = new File( "conf/plexus.properties" );
        }

        PropertiesFileContextFiller plexusPropertiesFiller =
            new PropertiesFileContextFiller( containerPropertiesFile, true );

        // add it to fillers as very 1st resource, and leaving others in
        request.getContextFillers().add( 0, plexusPropertiesFiller );

        AppContext appContext = ctxFactory.getAppContext( request );

        return appContext;
    }

    protected File initUpgradeBundleDirectory()
    {
        String filePath = System.getProperty( "buup.upgradeBundleDirectory" );

        File result = new File( filePath );

        if ( !result.isDirectory() )
        {
            throw new IllegalArgumentException(
                "The supplied upgradeBundleDirectory is not a directory or does not exists (upgradeBundleDirectory=\""
                    + filePath + "\")!" );
        }

        return result;
    }

    protected Map<String, String> initInvokerParameters()
    {
        HashMap<String, String> result = new HashMap<String, String>();

        for ( Object propertyKey : System.getProperties().keySet() )
        {
            String propertyKeyString = propertyKey.toString();

            if ( propertyKeyString.startsWith( "buup.parameter." ) )
            {
                result.put( propertyKeyString.substring( 15 ), System.getProperty( propertyKeyString ) );
            }
        }

        return result;
    }

    public boolean performUpgrade()
    {
        try
        {
            backupManager.backup();

            return doUpgrade();
        }
        catch ( Throwable e )
        {
            getLogger().error( "Error while performing upgrade!", e );

            try
            {
                backupManager.restore();

                return false;
            }
            catch ( IOException e1 )
            {
                getLogger().error( "Could not restore the previous state!", e1 );

                return false;
            }
        }

    }

    public boolean doUpgrade()
        throws IOException
    {
        return false;
    }

    // ==

    public void executeActions( ActionContext ctx, List<Action> actions )
        throws IOException
    {
        for ( Action action : actions )
        {
            try
            {
                action.perform( ctx );
            }
            catch ( Throwable e )
            {
                getLogger().error( "Action \"" + action.toString() + "\" thrown an error!", e );

                IOException wrappedE = new IOException( "Action \"" + action.toString() + "\" thrown an error!" );

                wrappedE.initCause( e );

                throw wrappedE;
            }
        }
    }

    // ==

    public void finishWithoutJobDone()
        throws IOException
    {
        // do a restore
        backupManager.restore();

        System.exit( 0 );
    }

    // ==

    public static void main( String[] args )
    {
        new Buup().upgrade();
    }
}
