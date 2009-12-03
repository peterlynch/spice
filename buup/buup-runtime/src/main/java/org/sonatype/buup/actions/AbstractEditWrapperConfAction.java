package org.sonatype.buup.actions;

import java.io.File;

import org.sonatype.buup.cfgfiles.DefaultPropertiesFile;
import org.sonatype.buup.cfgfiles.jsw.DefaultWrapperConfEditor;
import org.sonatype.buup.cfgfiles.jsw.WrapperConfEditor;

public abstract class AbstractEditWrapperConfAction
    extends AbstractFileManipulatorAction
{
    public void perform( ActionContext ctx )
        throws Exception
    {
        File src = getWrapperConfFile( ctx );

        WrapperConfEditor editor = null;

        if ( !ctx.containsKey( src ) )
        {
            File dest = ctx.getBuup().getBackupManager().editFile( src );

            editor = new DefaultWrapperConfEditor( new DefaultPropertiesFile( dest ) );

            ctx.put( src, editor );
        }
        else
        {
            editor = (WrapperConfEditor) ctx.get( src );
        }

        editWrapperConf( ctx, editor );

        editor.save();
    }

    public abstract File getWrapperConfFile( ActionContext ctx );

    public abstract void editWrapperConf( ActionContext ctx, WrapperConfEditor editor );

}
