package org.codehaus.plexus.swizzle;

import org.apache.http.impl.client.DefaultHttpClient;


public interface ProxyServerConfigurator
{
    void applyToClient( DefaultHttpClient client );
}
