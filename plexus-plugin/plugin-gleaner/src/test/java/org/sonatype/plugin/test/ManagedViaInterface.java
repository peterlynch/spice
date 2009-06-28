package org.sonatype.plugin.test;

import javax.inject.Inject;
import javax.inject.Named;

public class ManagedViaInterface
    implements ManagedInterface
{

    @Inject
    private ComponentManaged mangedComponent;

    @Inject
    private UserCustomComponent userCustomComponent;

    @Inject
    @Named( "another" )
    private UserCustomComponent userCustomComponentNamed;

    public ComponentManaged getMangedComponent()
    {
        return mangedComponent;
    }

    public UserCustomComponent getUserCustomComponent()
    {
        return userCustomComponent;
    }

    public UserCustomComponent getNamedUserCustomComponent()
    {
        return userCustomComponentNamed;
    }
}
