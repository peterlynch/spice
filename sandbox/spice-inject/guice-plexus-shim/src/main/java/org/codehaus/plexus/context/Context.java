package org.codehaus.plexus.context;

import java.util.Map;

public interface Context
{
    boolean contains( String key );

    void put( Object key, Object value );

    Map<?, ?> getContextData();
}
