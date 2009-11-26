package org.sonatype.buup;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextFactory;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.PropertiesFileContextFiller;
import org.sonatype.buup.backup.BackupManager;
import org.sonatype.buup.backup.DefaultBackupManager;
import org.sonatype.buup.recipe.Recipe;

public class Buup
{
    private Logger logger = LoggerFactory.getLogger( getClass() );

    public Logger getLogger()
    {
        return logger;
    }

    private AppContext appContext;

    private BackupManager backupManager;

    private WrapperHelper wrapperHelper;

    public AppContext getAppContext()
    {
        return appContext;
    }

    public WrapperHelper getWrapperHelper()
    {
        return wrapperHelper;
    }

    public boolean upgrade()
    {
        try
        {
            // create app context to have access to same values as bundle app has, with same transformations and
            // overrides
            appContext = createAppContext();

            backupManager = new DefaultBackupManager();

            wrapperHelper = new WrapperHelper( appContext.getBasedir() );

            return upgrade( null );
        }
        catch ( Throwable t )
        {
            return true;
        }
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

    public boolean upgrade( Recipe recipe )
    {
        try
        {
            backupManager.backup( recipe );

            return true;
        }
        catch ( Throwable e )
        {
            try
            {
                backupManager.restore( recipe );

                return true;
            }
            catch ( IOException e1 )
            {
                return false;
            }
        }

    }

    public static void main( String[] args )
    {
        new Buup().upgrade();
    }
}
