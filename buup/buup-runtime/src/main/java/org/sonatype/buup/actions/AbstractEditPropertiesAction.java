package org.sonatype.buup.actions;

import java.io.File;
import java.io.IOException;

import org.sonatype.buup.cfgfiles.DefaultPropertiesFile;
import org.sonatype.buup.cfgfiles.PropertiesFile;

public abstract class AbstractEditPropertiesAction
    extends AbstractFileManipulatorAction
{
    public void perform( ActionContext ctx )
        throws Exception
    {
        File src = getPropertiesFile( ctx );

        PropertiesFile propertiesFile = null;

        if ( !ctx.containsKey( src ) )
        {
            File dest = ctx.getBuup().getBackupManager().editFile( src );

            propertiesFile = new DefaultPropertiesFile( dest );

            ctx.put( src, propertiesFile );
        }
        else
        {
            propertiesFile = (PropertiesFile) ctx.get( src );
        }

        editPlexusProperties( ctx, propertiesFile );

        propertiesFile.save();
    }

    public abstract File getPropertiesFile( ActionContext ctx );

    public abstract void editPlexusProperties( ActionContext ctx, PropertiesFile file )
        throws IOException;
}
