package org.codehaus.plexus.component.composition;

import java.util.List;

import org.codehaus.plexus.component.repository.ComponentDescriptor;

public class DefaultCompositionResolver
    implements CompositionResolver
{
    public void addComponentDescriptor( ComponentDescriptor<?> componentDescriptor )
    {
        throw new UnsupportedOperationException();
    }

    public List getRequirements( String role, String roleHint )
    {
        throw new UnsupportedOperationException();
    }

    public List findRequirements( String role, String roleHint )
    {
        throw new UnsupportedOperationException();
    }
}
