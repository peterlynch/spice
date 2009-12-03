package org.sonatype.buup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
public abstract class Buup
{
    private Logger logger = LoggerFactory.getLogger( getClass() );

    public Logger getLogger()
    {
        return logger;
    }

    private AppContext appContext;

    private File upgradeBundleDirectory;

    private Map<String, String> parameters;

    private WrapperHelper wrapperHelper;

    private BackupManager backupManager;

    private boolean upgradeSucessful = false;

    public AppContext getAppContext()
    {
        return appContext;
    }

    public File getUpgradeBundleDirectory()
    {
        return upgradeBundleDirectory;
    }

    public File getUpgradeBundleContentDirectory()
    {
        return new File( getUpgradeBundleDirectory(), "content" );
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public WrapperHelper getWrapperHelper()
    {
        return wrapperHelper;
    }

    public BackupManager getBackupManager()
    {
        return backupManager;
    }

    public boolean upgrade()
    {
        getLogger().info( "Initializing BUUP..." );
        try
        {
            initialize();

            prepareEnvironment();

            upgradeSucessful = performUpgrade();

            return upgradeSucessful;
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

        wrapperHelper = new WrapperHelper( appContext.getBasedir() );

        backupManager = new DefaultBackupManager( this );

        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                shutdown();
            }
        } );
    }

    protected void prepareEnvironment()
        throws Exception
    {
        // undo what buup invoker did
        wrapperHelper.restoreWrapperConf();
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

        getLogger().info( "BUUP bundle is located at \"" + result.getAbsolutePath() + "\"..." );

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
        getLogger().info( "Performing actual upgrade..." );

        try
        {
            getBackupManager().backup();
        }
        catch ( IOException e )
        {
            getLogger().error( "Could not do a backup!", e );

            return false;
        }

        boolean succesful = false;

        try
        {
            succesful = doUpgrade();

            if ( succesful )
            {
                getLogger().info( "Upgrade finished succesfully." );

                getBackupManager().cleanup();
            }
        }
        catch ( Exception t )
        {
            getLogger().error( "Unexpected exception while doing upgrade!", t );

            succesful = false;
        }

        return succesful;
    }

    public boolean doUpgrade()
        throws Exception
    {
        ActionContext ctx = getActionContext();

        getAction().perform( ctx );

        return true;
    }

    public abstract ActionContext getActionContext();

    public abstract Action getAction();

    // ==

    public void shutdown()
    {
        if ( !upgradeSucessful )
        {
            backupManager.restore();
        }
    }

    // ==

    // public static void main( String[] args )
    // {
    // new Buup().upgrade();
    // }
}
