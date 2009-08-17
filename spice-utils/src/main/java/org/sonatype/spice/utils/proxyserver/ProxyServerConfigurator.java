package org.sonatype.spice.utils.proxyserver;

import org.apache.commons.httpclient.HttpClient;

public interface ProxyServerConfigurator
{
    void applyToClient( HttpClient client );
}
