package org.sonatype.jsecurity.realms.tools;

import org.sonatype.jsecurity.model.Configuration;

public class TestSecurityResource
    implements StaticSecurityResource
{
    public String getResourcePath()
    {
        return "/org/sonatype/jsecurity/configuration/static-merging/static-security.xml";
    }
    
    public Configuration getConfiguration()
    {
        return null;
    }
}
