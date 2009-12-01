package org.sonatype.buup.actions.nexus;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.sonatype.buup.actions.AbstractAction;
import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.cfgfiles.jsw.WrapperConfEditor;

public class SetBundleMemoryAction
    extends AbstractAction
{
    public static final String NEXUS_BUNDLE_MAX_HEAP_SIZE_KEY = "nexus.bundle.xmx";

    public static final String NEXUS_BUNDLE_INITIAL_HEAP_SIZE_KEY = "nexus.bundle.xms";

    public void perform( ActionContext ctx )
        throws Exception
    {
        WrapperConfEditor editor = ctx.getWrapperConfEditor();

        if ( ctx.getBuup().getParameters().containsKey( NEXUS_BUNDLE_INITIAL_HEAP_SIZE_KEY ) )
        {
            setJVMParameter( editor, "-Xms", ctx.getBuup().getParameters().get( NEXUS_BUNDLE_INITIAL_HEAP_SIZE_KEY ) );
        }

        if ( ctx.getBuup().getParameters().containsKey( NEXUS_BUNDLE_MAX_HEAP_SIZE_KEY ) )
        {
            setJVMParameter( editor, "-Xmx", ctx.getBuup().getParameters().get( NEXUS_BUNDLE_MAX_HEAP_SIZE_KEY ) );
        }

        editor.save();
    }

    // ==

    protected void setJVMParameter( WrapperConfEditor editor, String prefix, String value )
        throws IOException
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
