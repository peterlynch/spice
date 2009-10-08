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
package org.jsecurity.session.mgt.eis;

import org.jsecurity.session.Session;
import org.jsecurity.session.UnknownSessionException;

import java.io.Serializable;
import java.util.Collection;

/**
 * Data Access Object design pattern specification to enable {@link Session} access to an
 * EIS (Enterprise Information System).
 *
 * @author Les Hazlewood
 * @since 0.1
 */
public interface SessionDAO {

    /**
     * Inserts a new Session record into the underling EIS (e.g. Relational database, file system, mainframe,
     * etc, depending on the DAO implementation).
     * <p/>
     * After this method is invoked, the {@link org.jsecurity.session.Session#getId()}
     * method executed on the argument must return a valid session identifier.  That is, the following should
     * always be true:
     * <p/>
     * <code>Serializable id = create( session );<br/>
     * id.equals( session.getId() ) == true</code>
     *
     * <p>Implementations are free to throw any exceptions that might occur due to
     * integrity violation constraints or other EIS related errors.
     *
     * @param session the {@link Session} object to create in the EIS.
     * @return the EIS id (e.g. primary key) of the created <tt>Session</tt> object.
     */
    Serializable create(Session session);

    /**
     * Retrieves the session from the EIS uniquely identified by the specified
     * <tt>sessionId</tt>.
     *
     * @param sessionId the system-wide unique identifier of the Session object to retrieve from
     *                  the EIS.
     * @return the persisted session in the EIS identified by <tt>sessionId</tt>.
     * @throws UnknownSessionException if there is no EIS record for any session with the
     *                                 specified <tt>sessionId</tt>
     */
    Session readSession(Serializable sessionId) throws UnknownSessionException;

    /**
     * Updates (persists) data from a previously created Session instance in the EIS identified by
     * <tt>{@link Session#getId() session.getId()}</tt>.  This effectively propagates
     * the data in the argument to the EIS record previously saved.
     *
     * <p>Aside from the UnknownSessionException, implementations are free to throw any other
     * exceptions that might occur due to integrity violation constraints or other EIS related
     * errors.
     *
     * @param session the Session to update
     * @throws UnknownSessionException if no existing EIS session record exists with the
     *                                 identifier of {@link Session#getId() session.getSessionId()}
     */
    void update(Session session) throws UnknownSessionException;

    /**
     * Deletes the associated EIS record of the specified <tt>session</tt>.  If there never
     * existed a session EIS record with the identifier of
     * {@link Session#getId() session.getId()}, then this method does nothing.
     *
     * @param session the session to delete.
     */
    void delete(Session session);

    /**
     * Returns all sessions in the EIS that are considered active, meaning all sessions that
     * haven't been stopped/expired.  This is primarily used to validate potential orphans.
     *
     * If there are no active sessions in the EIS, this method may return an empty collection
     * or <tt>null</tt>.
     *
     * @return a Collection of <tt>Session</tt>s that are considered active, or an
     *         empty collection or <tt>null</tt> if there are no active sessions.
     */
    Collection<Session> getActiveSessions();
}
