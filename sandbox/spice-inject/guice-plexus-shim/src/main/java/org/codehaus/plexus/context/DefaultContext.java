package org.codehaus.plexus.context;

import java.util.Map;

public class DefaultContext
    implements Context
{
    public DefaultContext()
    {
        throw new UnsupportedOperationException();
    }

    public DefaultContext( Map<Object, Object> contextData )
    {
        throw new UnsupportedOperationException();
    }

    public boolean contains( Object key )
    {
        throw new UnsupportedOperationException();
    }

    public Object get( Object key )
        throws ContextException
    {
        throw new UnsupportedOperationException();
    }

    public void put( Object key, Object value )
        throws IllegalStateException
    {
        throw new UnsupportedOperationException();
    }

    public void hide( Object key )
        throws IllegalStateException
    {
        throw new UnsupportedOperationException();
    }

    public Map getContextData()
    {
        throw new UnsupportedOperationException();
    }

    public void makeReadOnly()
    {
        throw new UnsupportedOperationException();
    }

    public String toString()
    {
        throw new UnsupportedOperationException();
    }
}
