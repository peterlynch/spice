package org.codehaus.plexus.swizzle;

import org.apache.commons.httpclient.HttpClient;

public interface ProxyServerConfigurator
{
    void applyToClient( HttpClient client );
}
