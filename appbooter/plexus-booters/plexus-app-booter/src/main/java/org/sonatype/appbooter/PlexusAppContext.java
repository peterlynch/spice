package org.sonatype.appbooter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.sonatype.appcontext.AppContext;

/**
 * A thin wrapper that makes AppContext "plexus friendly", and Plexus uses the same AppContext as any other component
 * is.
 * 
 * @author cstamas
 */
public class PlexusAppContext
    implements Context
{
    /**
     * The wrapped app context.
     */
    private final AppContext appContext;

    /**
     * Is the containerContext read only.
     */
    private final AtomicBoolean readOnly = new AtomicBoolean( false );

    public PlexusAppContext( AppContext appContext )
    {
        this.appContext = appContext;
    }

    public AppContext getAppContext()
    {
        return appContext;
    }

    public boolean contains( Object key )
    {
        return getAppContext().containsKey( key ) && getAppContext().get( key ) != null;
    }

    public Object get( Object key )
        throws ContextException
    {
        Object data = getAppContext().get( key );

        if ( data == null )
        {
            // There is no data for the key
            throw new ContextException( "Unable to resolve context key: " + key );
        }

        return data;
    }

    public void put( Object key, Object value )
        throws IllegalStateException
    {
        checkWriteable();

        // check for a null key
        if ( key == null )
        {
            throw new IllegalArgumentException( "Key is null" );
        }

        if ( value == null )
        {
            getAppContext().remove( key );
        }
        else
        {
            getAppContext().put( key, value );
        }
    }

    public Map<Object, Object> getContextData()
    {
        return Collections.unmodifiableMap( getAppContext() );
    }

    public void hide( Object key )
        throws IllegalStateException
    {
        checkWriteable();

        getAppContext().remove( key );
    }

    public void makeReadOnly()
    {
        readOnly.set( true );
    }

    // ==

    protected void checkWriteable()
        throws IllegalStateException
    {
        if ( readOnly.get() )
        {
            throw new IllegalStateException( "Context is read only and can not be modified" );
        }
    }

}
