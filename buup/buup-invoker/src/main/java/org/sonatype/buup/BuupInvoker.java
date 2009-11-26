package org.sonatype.buup;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.buup.cfgfiles.jsw.WrapperConfEditor;
import org.sonatype.buup.cfgfiles.jsw.WrapperHelper;

public class BuupInvoker
{
    private Logger logger = LoggerFactory.getLogger( BuupInvoker.class );

    protected Logger getLogger()
    {
        return logger;
    }

    public void invoke( BuupInvocationRequest request )
        throws BuupInvocationException
    {
        validateRequest( request );

        WrapperHelper wrapperHelper = new WrapperHelper( request.getBasedir() );

        boolean backupDone = false;

        try
        {
            // 1st, forcedly backup the configuration
            backupDone = wrapperHelper.backupWrapperConf( true );

            // 2nd, get an jsw config editor (we have a backup, now we modify the user edited one)
            WrapperConfEditor editor = wrapperHelper.getWrapperConfEditor();

            // 3rd, swap in a nice alternative app
            editor.setWrapperJavaMainclass( "org.sonatype.buup.Buup" );

            // 4th, communicate with buup
            editor.addWrapperJavaAdditional( "-Dbuup.upgradeBundleDirectory="
                + request.getUpgradeBundleDirectory().getAbsolutePath() );

            // pass over any param
            for ( Map.Entry<String, String> entry : request.getParameters().entrySet() )
            {
                editor.addWrapperJavaAdditional( "-Dbuup.parameter." + entry.getKey() + "=" + entry.getValue() );
            }

            // save
            editor.save();

            // STOP JVM (and wrapper will bring up BUUP)
            System.exit( 0 );
        }
        catch ( Throwable t )
        {
            getLogger().error( "Cannot invoke BUUP!", t );

            if ( backupDone )
            {
                try
                {
                    wrapperHelper.restoreWrapperConf();
                }
                catch ( IOException e )
                {
                    // wow, we have now an original throwable that made us to restore, but restore died also...
                    // what to report? Original t reported above, so let's nag about IOException
                    getLogger().error( "Cannot restore Bundle JSW configuration!", e );
                }
            }

            throw new BuupInvocationException( "Unexpected error while invoking BUUP", t );
        }
    }

    protected void validateRequest( BuupInvocationRequest request )
        throws BuupInvocationException
    {
        if ( !request.getBasedir().isDirectory() )
        {
            throw new BuupInvocationException(
                "The basedir where request points is not a directory or does not exists (basedir=\""
                    + request.getBasedir().getAbsolutePath() + "\")!" );
        }

        if ( !request.getUpgradeBundleDirectory().isDirectory() )
        {
            throw new BuupInvocationException(
                "The exploded upgrade bundle where request points is not a directory or does not exists (upgradeBundleDirectory=\""
                    + request.getUpgradeBundleDirectory().getAbsolutePath() + "\")!" );
        }
    }
}
