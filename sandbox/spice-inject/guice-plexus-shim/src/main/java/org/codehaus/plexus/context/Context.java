package org.codehaus.plexus.context;

import java.util.Map;

public interface Context
{
    void put( Object key, Object value );

    Map<?, ?> getContextData();
}
