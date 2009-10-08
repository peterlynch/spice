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
package org.jsecurity.session;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;

/**
 * Simple <code>Session</code> implementation that immediately delegates all corresponding calls to an
 * underlying proxied session instance.
 * <p/>
 * This class is mostly useful for framework subclassing to intercept certain <code>Session</code> calls
 * and perform additional logic.
 *
 * @author Les Hazlewood
 * @since 0.9
 */
public class ProxiedSession implements Session {

    /**
     * The proxied instance
     */
    protected final Session proxy;

    /**
     * Constructs an instance that proxies the specified <code>target</code>.  Subclasses may access this
     * target via the <code>protected final 'proxy'</code> attribute, i.e. <code>this.proxy</code>.
     *
     * @param target the specified target <code>Session</code> to proxy.
     */
    public ProxiedSession(Session target) {
        if (target == null) {
            throw new IllegalArgumentException("Target session to proxy cannot be null.");
        }
        proxy = target;
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public Serializable getId() {
        return proxy.getId();
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public Date getStartTimestamp() {
        return proxy.getStartTimestamp();
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public Date getLastAccessTime() {
        return proxy.getLastAccessTime();
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public long getTimeout() throws InvalidSessionException {
        return proxy.getTimeout();
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public void setTimeout(long maxIdleTimeInMillis) throws InvalidSessionException {
        proxy.setTimeout(maxIdleTimeInMillis);
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public InetAddress getHostAddress() {
        return proxy.getHostAddress();
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public void touch() throws InvalidSessionException {
        proxy.touch();
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public void stop() throws InvalidSessionException {
        proxy.stop();
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public Collection<Object> getAttributeKeys() throws InvalidSessionException {
        return proxy.getAttributeKeys();
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public Object getAttribute(Object key) throws InvalidSessionException {
        return proxy.getAttribute(key);
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public void setAttribute(Object key, Object value) throws InvalidSessionException {
        proxy.setAttribute(key, value);
    }

    /**
     * Immediately delegates to the underlying proxied session.
     */
    public Object removeAttribute(Object key) throws InvalidSessionException {
        return proxy.removeAttribute(key);
    }

}
