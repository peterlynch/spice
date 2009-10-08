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
package org.jsecurity;

import java.io.Serializable;

/**
 * Root exception for all JSecurity runtime exceptions.  This class is used as the root instead
 * of {@link java.lang.SecurityException} to remove the potential for conflicts;  many other
 * frameworks and products (such as J2EE containers) perform special operations when
 * encountering {@link java.lang.SecurityException}.
 *
 * @author Les Hazlewood
 * @since 0.1
 */
public class JSecurityException extends RuntimeException implements Serializable {

    /**
     * Creates a new JSecurityException.
     */
    public JSecurityException() {
        super();
    }

    /**
     * Constructs a new JSecurityException.
     *
     * @param message the reason for the exception
     */
    public JSecurityException(String message) {
        super(message);
    }

    /**
     * Constructs a new JSecurityException.
     *
     * @param cause the underlying Throwable that caused this exception to be thrown.
     */
    public JSecurityException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new JSecurityException.
     *
     * @param message the reason for the exception
     * @param cause   the underlying Throwable that caused this exception to be thrown.
     */
    public JSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

}
