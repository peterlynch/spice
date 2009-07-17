package org.codehaus.plexus.swizzle;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public class DefaultProxyServerConfigurator
    implements ProxyServerConfigurator
{
    public void applyToClient( HttpClient client )
    {
        String proxySet = System.getProperty("http.proxySet");
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        String proxyUser = System.getProperty("http.proxyUserName");
        String proxyPass = System.getProperty("http.proxyPassword");
        
        if (!Boolean.TRUE.toString().equals(proxySet) || proxyHost == null || proxyPort == null) {
            return;
        }
        
        client.getHostConfiguration().setProxy(proxyHost, Integer.parseInt(proxyPort));

        if (proxyUser != null) {
          Credentials cred = new UsernamePasswordCredentials(proxyUser, proxyPass);
          client.getState().setProxyCredentials(AuthScope.ANY, cred);
        }
    }
}
