package org.codehaus.plexus.context;

import java.util.Map;

public final class DefaultContext
    implements Context
{
    public DefaultContext()
    {
    }

    @SuppressWarnings( "unused" )
    public DefaultContext( final Map<?, ?> context )
    {
    }

    public boolean contains( final String key )
    {
        return false;
    }

    public void put( final Object key, final Object value )
    {
    }

    public Map<?, ?> getContextData()
    {
        return null;
    }
}
