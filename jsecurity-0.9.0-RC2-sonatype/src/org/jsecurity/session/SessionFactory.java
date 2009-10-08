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

import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.HostUnauthorizedException;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * A <tt>SessionFactory</tt> is responsible for starting new {@link Session Session}s and
 * acquiring existing {@link Session Session}s.
 *
 * @author Les Hazlewood
 * @since 0.1
 */
public interface SessionFactory {

    /**
     * Starts a new session within the system for the host with the specified
     * originating IP address.
     *
     * <p>An implementation of this interface may be configured to allow a <tt>null</tt> argument,
     * thereby indicating the originating IP is either unknown or has been
     * explicitly omitted by the caller.  However, if the implementation is configured to require
     * a valid <tt>hostAddress</tt> and the argument is <tt>null</tt>, an
     * {@link IllegalArgumentException IllegalArgumentException} will be thrown.
     *
     * <p>In web-based systems, this InetAddress can be inferred from the
     * {@link javax.servlet.ServletRequest#getRemoteAddr() javax.servlet.ServletRequest.getRemoteAddr()}
     * method, or in socket-based systems, it can be obtained via inspecting the socket
     * initiator's host IP.
     *
     * <p>Most secure environments <em>should</em> require that a valid, non-<tt>null</tt>
     * <tt>hostAddress</tt> be specified, since knowing the <tt>hostAddress</tt> allows for more
     * flexibility when securing a system: by requiring an InetAddress, access control policies
     * can also ensure access is restricted to specific client <em>locations</em> in
     * addition to user principals, if so desired.
     *
     * <p><b>Caveat</b> - if clients to your system are on a
     * public network (as would be the case for a public web site), odds are high the clients can be
     * behind a NAT (Network Address Translation) router or HTTP proxy server.  If so, all clients
     * accessing your system behind that router or proxy will have the same originating IP address.
     * If your system is configured to allow only one session per IP, then the next request from a
     * different NAT or proxy client will fail and access will be deny for that client.  Just be
     * aware that ip-based security policies are best utilized in LAN or private WAN environments
     * when you can be ensure clients will not share IPs or be behind such NAT routers or
     * proxy servers.
     *
     * @param hostAddress the originating host InetAddress of the external party
     *                    (user, 3rd party product, etc) that is attempting to interact with the system.
     * @return a handle to the newly created session.
     * @throws HostUnauthorizedException if the system access control policy restricts access based
     *                                   on client location/IP and the specified hostAddress hasn't been enabled.
     * @throws IllegalArgumentException  if the system is configured to require a valid,
     *                                   non-<tt>null</tt> argument and the specified <tt>hostAddress</tt> is null.
     */
    Session start(InetAddress hostAddress) throws HostUnauthorizedException, IllegalArgumentException;

    /**
     * Acquires a handle to the session identified by the specified <tt>sessionId</tt>.
     *
     * <p><b>Although simple, this method finally enables behavior absent in Java for years:</b>
     *
     * <p>the
     * ability to participate in a server-side session across clients of different mediums,
     * such as web appliations, Java applets, standalone C# clients over XMLRPC and/or SOAP, and
     * many others.  This is a <em>huge</em> benefit in heterogeneous enterprise applications.
     *
     * <p>To maintain session integrity across client mediums, the sessionId must be transmitted
     * to all client mediums securely (e.g. over SSL) to prevent man-in-the-middle attacks.  This
     * is nothing new - all web applications are susceptible to the same problem when transmitting
     * {@link javax.servlet.http.Cookie Cookie}s or when using URL rewriting.  As long as the
     * <tt>sessionId</tt> is transmitted securely, session integrity can be maintained.
     *
     * @param sessionId the id of the session to acquire.
     * @return a handle to the session identified by <tt>sessionId</tt>
     * @throws InvalidSessionException if the session identified by <tt>sessionId</tt> has
     *                                 been stopped, expired, or doesn't exist.
     * @throws AuthorizationException  if the executor of this method is not allowed to acquire
     *                                 (i.e. join) the session identified by <tt>sessionId</tt>.  The reason for the exception
     *                                 is implementation specific and could be for any number of reasons.  A common reason in many
     *                                 systems would be if one host tried to acquire/join a session that originated on an entirely
     *                                 different host (although it is not a JSecurity requirement this scenario is disallowed -
     *                                 its just an example that <em>may</em> throw an Exception in many systems).
     * @see HostUnauthorizedException
     */
    Session getSession(Serializable sessionId) throws InvalidSessionException, AuthorizationException;

}
