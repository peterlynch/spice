package org.sonatype.buup.actions;

import java.util.ArrayList;
import java.util.List;

public class ActionList
    extends AbstractAction
{
    private List<Action> actions;

    public List<Action> getActions()
    {
        if ( actions == null )
        {
            actions = new ArrayList<Action>();
        }

        return actions;
    }

    public void setActions( List<Action> actions )
    {
        this.actions = actions;
    }

    public void perform( ActionContext ctx )
        throws Exception
    {
        List<Action> actions = getActions();

        int i = 1;

        for ( Action action : actions )
        {
            getLogger().info( "Performing action " + action.getClass().getName() + " (" + i++ + "/" + actions.size() + ")." );

            action.perform( ctx );
        }

        getLogger().info( "ActionList list finished succesfully " + actions.size() + " actions." );
    }
}
