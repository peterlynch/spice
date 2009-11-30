package org.sonatype.buup.actions;

import java.io.File;
import java.util.ArrayList;

import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.buup.Buup;

/**
 * This class scans the basedir (configurable, defaults to upgradeBundleDirectory and include pattern *.jar) directory
 * for given patterns, and copies those into the target (defaults to app basedir). The bundle directories are "adjusted"
 * to be remapped from basedir. This action has the "overwrite" flag, that is checked in batch mode before any copy is
 * tried (target file existence is checked). For patterns, plexus java utils DirectoryScanner is used, and look what
 * patterns are usable in there.
 * 
 * @author cstamas
 */
public class CopyFilesToPlaceAction
    extends AbstractFileManipulatorAction
{
    public File basedir;

    public File targetDir;

    public boolean overwrite = false;

    public String[] includes = new String[] { "**/*.jar" };

    public String[] excludes = DirectoryScanner.DEFAULTEXCLUDES;

    public CopyFilesToPlaceAction( Buup buup )
    {
        super( buup );

        setBasedir( getBuup().getUpgradeBundleDirectory() );

        setTargetDir( getBuup().getAppContext().getBasedir() );
    }

    public File getBasedir()
    {
        return basedir;
    }

    public void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }

    public File getTargetDir()
    {
        return targetDir;
    }

    public void setTargetDir( File targetDir )
    {
        this.targetDir = targetDir;
    }

    public boolean isOverwrite()
    {
        return overwrite;
    }

    public void setOverwrite( boolean overwrite )
    {
        this.overwrite = overwrite;
    }

    public String[] getIncludes()
    {
        return includes;
    }

    public void setIncludes( String[] includes )
    {
        this.includes = includes;
    }

    public String[] getExcludes()
    {
        return excludes;
    }

    public void setExcludes( String[] excludes )
    {
        this.excludes = excludes;
    }

    public void perform( ActionContext ctx )
        throws Exception
    {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir( getBasedir() );

        scanner.setIncludes( getIncludes() );

        scanner.setExcludes( getExcludes() );

        scanner.scan();

        ArrayList<File> sourceFiles = new ArrayList<File>();
        ArrayList<File> destinationFiles = new ArrayList<File>();

        for ( String fileName : scanner.getIncludedFiles() )
        {
            sourceFiles.add( resolveChildPath( getBasedir(), fileName ) );
            destinationFiles.add( resolveChildPath( getTargetDir(), fileName ) );
        }

        // check is any of these exists
        boolean existsAny = filesExists( destinationFiles, false );

        if ( !isOverwrite() && existsAny )
        {
            throw ioexception( "Some of the files to copy already exists, and cannot overwrite them!" );
        }

        for ( int i = 0; i < sourceFiles.size(); i++ )
        {
            copyFile( sourceFiles.get( i ), destinationFiles.get( i ), isOverwrite() );
        }
    }
}
