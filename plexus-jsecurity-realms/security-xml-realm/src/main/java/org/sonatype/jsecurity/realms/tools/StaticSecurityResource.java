package org.sonatype.jsecurity.realms.tools;

import org.sonatype.jsecurity.model.Configuration;

public interface StaticSecurityResource
{
    String getResourcePath();
    
    Configuration getConfiguration();
}
