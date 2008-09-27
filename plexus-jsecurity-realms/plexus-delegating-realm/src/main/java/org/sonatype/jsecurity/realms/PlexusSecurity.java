package org.sonatype.jsecurity.realms;

import org.jsecurity.mgt.SecurityManager;

public interface PlexusSecurity
    extends SecurityManager
{
    String ROLE = PlexusSecurity.class.getName();
}
