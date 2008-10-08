package org.sonatype.jsecurity.web;

public interface PlexusMutableWebConfiguration
    extends PlexusWebConfiguration
{
    void addProtectedResource( String pathPattern, String filterExpression )
        throws SecurityConfigurationException;
}
