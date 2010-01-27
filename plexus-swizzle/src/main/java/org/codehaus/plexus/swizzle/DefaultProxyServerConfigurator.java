/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.codehaus.plexus.swizzle;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.sonatype.spice.utils.proxyserver.ProxyServerConfigurator;

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
