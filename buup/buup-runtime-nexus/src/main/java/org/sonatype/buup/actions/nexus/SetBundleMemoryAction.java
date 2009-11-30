package org.sonatype.buup.actions.nexus;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.sonatype.buup.actions.AbstractAction;
import org.sonatype.buup.actions.ActionContext;
import org.sonatype.buup.cfgfiles.jsw.WrapperConfEditor;
import org.sonatype.buup.nexus.NexusBuup;

public class SetBundleMemoryAction
    extends AbstractAction
{
    public static final String NEXUS_BUNDLE_MAX_HEAP_SIZE_KEY = "nexus.bundle.xmx";

    public static final String NEXUS_BUNDLE_INITIAL_HEAP_SIZE_KEY = "nexus.bundle.xms";

    public SetBundleMemoryAction( NexusBuup buup )
    {
        super( buup );
    }

    public void perform( ActionContext ctx )
        throws Exception
    {
        // look for nexus.jvm.memory param, if not found exit
        // if exists, get it
        // prepare JVM invocation param
        // get wrapper.conf editor
        // check for existing line with those params
        // update/add it
        WrapperConfEditor editor = getBuup().getWrapperHelper().getWrapperConfEditor();

        if ( getBuup().getParameters().containsKey( NEXUS_BUNDLE_INITIAL_HEAP_SIZE_KEY ) )
        {
            setJVMParameter( editor, "-Xms", getBuup().getParameters().get( NEXUS_BUNDLE_INITIAL_HEAP_SIZE_KEY ) );
        }

        if ( getBuup().getParameters().containsKey( NEXUS_BUNDLE_MAX_HEAP_SIZE_KEY ) )
        {
            setJVMParameter( editor, "-Xmx", getBuup().getParameters().get( NEXUS_BUNDLE_INITIAL_HEAP_SIZE_KEY ) );
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
                jvmParamsIterator.remove();
            }
        }

        jvmParams.add( prefix + value );

        editor.setWrapperJavaAdditional( jvmParams );
    }
}
