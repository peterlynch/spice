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
package org.sonatype.micromailer.imp;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 * This Trust Manager is "naive" because it trusts everyone.
 * 
 * @see http://www.howardism.org/Technical/Java/SelfSignedCerts.html
 **/
public class NaiveTrustManager
    implements X509TrustManager
{
    /**
     * Doesn't throw an exception, so this is how it approves a certificate.
     * 
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], String)
     **/
    public void checkClientTrusted( X509Certificate[] cert, String authType )
        throws CertificateException
    {
    }

    /**
     * Doesn't throw an exception, so this is how it approves a certificate.
     * 
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], String)
     **/
    public void checkServerTrusted( X509Certificate[] cert, String authType )
        throws CertificateException
    {
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     **/
    public X509Certificate[] getAcceptedIssuers()
    {
        return new X509Certificate[0];
    }
}
