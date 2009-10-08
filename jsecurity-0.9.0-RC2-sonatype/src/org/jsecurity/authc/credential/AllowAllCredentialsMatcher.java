/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jsecurity.authc.credential;

import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;

/**
 * A credentials matcher that always returns <tt>true</tt> when matching credentials no matter what arguments
 * are passed in.  This can be used for testing or when credentials are implicitly trusted for a particular
 * {@link org.jsecurity.realm.Realm Realm}.
 *
 * @author Jeremy Haile
 * @author Les Hazlewood
 * @since 0.2
 */
public class AllowAllCredentialsMatcher implements CredentialsMatcher {

    /**
     * Returns <code>true</code> <em>always</em> no matter what the method arguments are.
     *
     * @param token   the token submitted for authentication.
     * @param info    the account being verified for access
     * @return <code>true</code> <em>always</em>.
     */
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        return true;
    }
}
