package org.sonatype.buup.actions;

import org.sonatype.buup.Buup;

public class ActionFactory
{
    private final Buup buup;

    public ActionFactory( Buup buup )
    {
        this.buup = buup;
    }

    public Buup getBuup()
    {
        return buup;
    }

    public Action createAction( Class<Action> actionClass )
    {
        return null;
    }
}
