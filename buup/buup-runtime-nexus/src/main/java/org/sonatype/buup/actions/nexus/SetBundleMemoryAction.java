package org.sonatype.buup.actions.nexus;

import java.util.Iterator;
import java.util.List;

import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.cfgfiles.jsw.WrapperConfEditor;

public class SetBundleMemoryAction
    extends AbstractEditNexusWrapperConfAction
{
    public static final String NEXUS_BUNDLE_MAX_HEAP_SIZE_KEY = "nexus.bundle.xmx";

    public static final String NEXUS_BUNDLE_INITIAL_HEAP_SIZE_KEY = "nexus.bundle.xms";

    @Override
    public void editWrapperConf( ActionContext ctx, WrapperConfEditor editor )
    {
        if ( ctx.getParameters().containsKey( NEXUS_BUNDLE_INITIAL_HEAP_SIZE_KEY ) )
        {
            setJVMParameter( editor, "-Xms", ctx.getParameters().get( NEXUS_BUNDLE_INITIAL_HEAP_SIZE_KEY ) );
        }

        if ( ctx.getParameters().containsKey( NEXUS_BUNDLE_MAX_HEAP_SIZE_KEY ) )
        {
            setJVMParameter( editor, "-Xmx", ctx.getParameters().get( NEXUS_BUNDLE_MAX_HEAP_SIZE_KEY ) );
        }
    }

    // ==

    protected void setJVMParameter( WrapperConfEditor editor, String prefix, String value )
    {
        List<String> jvmParams = editor.getWrapperJavaAdditional();

        for ( Iterator<String> jvmParamsIterator = jvmParams.iterator(); jvmParamsIterator.hasNext(); )
        {
            String jvmParam = jvmParamsIterator.next();

            if ( jvmParam.startsWith( prefix ) )
            {
                jvmParamsIterator.remove(); // no break, to remove ALL if there are by mistake
            }
        }

        jvmParams.add( prefix + value );

        editor.setWrapperJavaAdditional( jvmParams );
    }
}
