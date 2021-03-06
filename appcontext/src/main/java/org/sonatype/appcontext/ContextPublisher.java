package org.sonatype.appcontext;

/**
 * A publisher to publish the Application context somewhere.
 * 
 * @author cstamas
 */
public interface ContextPublisher
{
    void publishContext( AppContextRequest request, AppContext context )
        throws AppContextException;
}
