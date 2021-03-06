package org.sonatype.appcontext;

import java.util.Map;

/**
 * A source to fill the Application Context.
 * 
 * @author cstamas
 */
public interface ContextFiller
{
    void fillContext( AppContextRequest request, Map<Object, Object> context )
        throws AppContextException;
}
