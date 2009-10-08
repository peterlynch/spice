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
package org.jsecurity.crypto.hash;

import org.jsecurity.codec.Base64;
import org.jsecurity.codec.Hex;

/**
 * Generates an SHA-256 Hash from a given input <tt>source</tt> with an optional <tt>salt</tt> and hash iterations.
 *
 * <p>See the {@link AbstractHash AbstractHash} parent class JavaDoc for a detailed explanation of Hashing
 * techniques and how the overloaded constructors function.
 *
 * <p><b>JDK Version Note</b> - Attempting to instantiate this class on JREs prior to version 1.4.0 will throw
 * an {@link IllegalStateException IllegalStateException}
 *
 * @author Les Hazlewood
 * @since 0.9
 */
public class Sha256Hash extends AbstractHash {

    public static final String ALGORITHM_NAME = "SHA-256";

    public Sha256Hash() {
    }

    public Sha256Hash(Object source) {
        super(source);
    }

    public Sha256Hash(Object source, Object salt) {
        super(source, salt);
    }

    public Sha256Hash(Object source, Object salt, int hashIterations) {
        super(source, salt, hashIterations);
    }

    protected String getAlgorithmName() {
        return ALGORITHM_NAME;
    }

    public static Sha256Hash fromHexString(String hex) {
        Sha256Hash hash = new Sha256Hash();
        hash.setBytes(Hex.decode(hex));
        return hash;
    }

    public static Sha256Hash fromBase64String(String base64) {
        Sha256Hash hash = new Sha256Hash();
        hash.setBytes(Base64.decode(base64));
        return hash;
    }


}
