package org.sonatype.buup.actions;

public interface Action
{
    void perform( ActionContext ctx )
        throws Exception;
}
