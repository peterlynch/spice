package org.codehaus.plexus;

public final class DefaultPlexusContainer
    implements PlexusContainer
{
    @SuppressWarnings( "unused" )
    public DefaultPlexusContainer( final ContainerConfiguration configuration )
        throws PlexusContainerException
    {
    }

    public Object lookup( final String role )
    {
        return null;
    }

    public Object lookup( final String role, final String roleHint )
    {
        return null;
    }

    public <T> T lookup( final Class<T> type )
    {
        return null;
    }

    public <T> T lookup( final Class<T> type, final String roleHint )
    {
        return null;
    }

    public void release( final Object component )
    {
    }

    public void dispose()
    {
    }
}
