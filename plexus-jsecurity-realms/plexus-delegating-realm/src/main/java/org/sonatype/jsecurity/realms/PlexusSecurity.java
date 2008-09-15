package org.sonatype.jsecurity.realms;

import java.util.Set;

import org.jsecurity.mgt.SecurityManager;

public interface PlexusSecurity
    extends SecurityManager
{
    void clearCache( String realmName );
    void clearCache( Set<String> realmNames );
}
