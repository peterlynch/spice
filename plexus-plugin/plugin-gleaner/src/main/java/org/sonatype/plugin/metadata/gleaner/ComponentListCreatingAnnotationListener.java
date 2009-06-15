package org.sonatype.plugin.metadata.gleaner;

import java.util.ArrayList;
import java.util.List;


public class ComponentListCreatingAnnotationListener
    implements AnnotationListener
{

    private List<String> componentClassNames = new ArrayList<String>();

    public void processEvent( AnnotationListernEvent event )
    {
        String className = event.getClassName();
        className = className.substring( 0, className.lastIndexOf( ".class" ) );
        className = className.replaceAll( "/", "." );
        this.componentClassNames.add( className );
    }

    public List<String> getComponentClassNames()
    {
        return componentClassNames;
    }

}
