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
        
        this.componentClassNames.add( className );
    }

    public List<String> getComponentClassNames()
    {
        return componentClassNames;
    }

}
