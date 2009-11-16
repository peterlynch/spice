package org.codehaus.plexus;

public interface PlexusContainer
{
    Object lookup( String role );

    Object lookup( String role, String roleHint );

    <T> T lookup( Class<T> type );

    <T> T lookup( Class<T> type, String roleHint );

    void release( Object component );

    void dispose();
}
