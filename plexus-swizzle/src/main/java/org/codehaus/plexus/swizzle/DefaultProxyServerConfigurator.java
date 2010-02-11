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

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

public class DefaultProxyServerConfigurator
    implements ProxyServerConfigurator
{
    public void applyToClient( DefaultHttpClient client )
    {
        String proxySet = System.getProperty("http.proxySet");
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        String proxyUser = System.getProperty("http.proxyUserName");
        String proxyPass = System.getProperty("http.proxyPassword");
        
        if (!Boolean.TRUE.toString().equals(proxySet) || proxyHost == null || proxyPort == null) {
            return;
        }
        
        HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort)); 
        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        
        if (proxyUser != null) {
        	
            client.getCredentialsProvider().setCredentials(
                    new AuthScope(proxyHost, Integer.parseInt(proxyPort)), 
                    new UsernamePasswordCredentials(proxyUser, proxyPass));
        }
    }
}
