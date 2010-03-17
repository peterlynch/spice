/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.util.Series;

import com.noelios.restlet.http.HttpCall;
import com.noelios.restlet.http.HttpConstants;
import com.noelios.restlet.http.HttpRequest;
import com.noelios.restlet.http.HttpServerCall;

/**
 * Call that is used by the Servlet HTTP server connector.
 * 
 * @author Jerome Louvel
 */
public class ServletCall extends HttpServerCall {

    /**
     * Returns the Servlet request that was used to generate the given Restlet
     * request.
     * 
     * @param request
     *            The Restlet request.
     * @return The Servlet request or null.
     */
    public static HttpServletRequest getRequest(Request request) {
        HttpServletRequest result = null;

        if (request instanceof HttpRequest) {
            final HttpCall httpCall = ((HttpRequest) request).getHttpCall();

            if (httpCall instanceof ServletCall) {
                result = ((ServletCall) httpCall).getRequest();
            }
        }

        return result;
    }

    /** The HTTP Servlet request to wrap. */
    private volatile HttpServletRequest request;

    /** The request headers. */
    private volatile Series<Parameter> requestHeaders;

    /** The HTTP Servlet response to wrap. */
    private volatile HttpServletResponse response;

    /**
     * Constructor.
     * 
     * @param serverAddress
     *            The server IP address.
     * @param serverPort
     *            The server port.
     * @param request
     *            The Servlet request
     * @param response
     *            The Servlet response.
     */
    public ServletCall(String serverAddress, int serverPort,
            HttpServletRequest request, HttpServletResponse response) {
        super(serverAddress, serverPort);
        this.request = request;
        this.response = response;
    }

    /**
     * Constructor.
     * 
     * @param server
     *            The parent server.
     * @param request
     *            The HTTP Servlet request to wrap.
     * @param response
     *            The HTTP Servlet response to wrap.
     */
    public ServletCall(Server server, HttpServletRequest request,
            HttpServletResponse response) {
        super(server);
        this.request = request;
        this.response = response;
    }

    @Override
    public String getClientAddress() {
        return getRequest().getRemoteAddr();
    }

    @Override
    public int getClientPort() {
        return getRequest().getRemotePort();
    }

    /**
     * Returns the server domain name.
     * 
     * @return The server domain name.
     */
    @Override
    public String getHostDomain() {
        return getRequest().getServerName();
    }

    /**
     * Returns the request method.
     * 
     * @return The request method.
     */
    @Override
    public String getMethod() {
        return getRequest().getMethod();
    }

    /**
     * Returns the server protocol.
     * 
     * @return The server protocol.
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.valueOf(getRequest().getScheme());
    }

    /**
     * Returns the HTTP Servlet request.
     * 
     * @return The HTTP Servlet request.
     */
    public HttpServletRequest getRequest() {
        return this.request;
    }

    @Override
    public ReadableByteChannel getRequestEntityChannel(long size) {
        // Can't do anything
        return null;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        try {
            return getRequest().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public ReadableByteChannel getRequestHeadChannel() {
        // Not available
        return null;
    }

    /**
     * Returns the list of request headers.
     * 
     * @return The list of request headers.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Series<Parameter> getRequestHeaders() {
        if (this.requestHeaders == null) {
            this.requestHeaders = new Form();

            // Copy the headers from the request object
            String headerName;
            String headerValue;
            for (final Enumeration<String> names = getRequest()
                    .getHeaderNames(); names.hasMoreElements();) {
                headerName = names.nextElement();
                for (final Enumeration<String> values = getRequest()
                        .getHeaders(headerName); values.hasMoreElements();) {
                    headerValue = values.nextElement();
                    this.requestHeaders.add(new Parameter(headerName,
                            headerValue));
                }
            }
        }

        return this.requestHeaders;
    }

    @Override
    public InputStream getRequestHeadStream() {
        // Not available
        return null;
    }

    /**
     * Returns the full request URI.
     * 
     * @return The full request URI.
     */
    @Override
    public String getRequestUri() {
        final String queryString = getRequest().getQueryString();

        if ((queryString == null) || (queryString.equals(""))) {
            return getRequest().getRequestURI();
        } else {
            return getRequest().getRequestURI() + '?' + queryString;
        }
    }

    /**
     * Returns the HTTP Servlet response.
     * 
     * @return The HTTP Servlet response.
     */
    public HttpServletResponse getResponse() {
        return this.response;
    }

    /**
     * Returns the response channel if it exists, null otherwise.
     * 
     * @return The response channel if it exists, null otherwise.
     */
    @Override
    public WritableByteChannel getResponseEntityChannel() {
        // Can't do anything
        return null;
    }

    /**
     * Returns the response stream if it exists, null otherwise.
     * 
     * @return The response stream if it exists, null otherwise.
     */
    @Override
    public OutputStream getResponseEntityStream() {
        try {
            return getResponse().getOutputStream();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Returns the response address.<br>
     * Corresponds to the IP address of the responding server.
     * 
     * @return The response address.
     */
    @Override
    public String getServerAddress() {
        return getRequest().getLocalAddr();
    }

    /**
     * Returns the server port.
     * 
     * @return The server port.
     */
    @Override
    public int getServerPort() {
        return getRequest().getServerPort();
    }

    @Override
    public String getSslCipherSuite() {
        return (String) getRequest().getAttribute(
                "javax.servlet.request.cipher_suite");
    }

    @Override
    public List<Certificate> getSslClientCertificates() {
        final Certificate[] certificateArray = (Certificate[]) getRequest()
                .getAttribute("javax.servlet.request.X509Certificate");
        if (certificateArray != null) {
            return Arrays.asList(certificateArray);
        } else {
            return null;
        }
    }

    @Override
    public Integer getSslKeySize() {
        Integer keySize = (Integer) getRequest().getAttribute(
                "javax.servlet.request.key_size");
        if (keySize == null) {
            keySize = super.getSslKeySize();
        }
        return keySize;
    }

    @Override
    public String getVersion() {
        String result = null;
        final int index = getRequest().getProtocol().indexOf('/');

        if (index != -1) {
            result = getRequest().getProtocol().substring(index + 1);
        }

        return result;
    }

    /**
     * Indicates if the request was made using a confidential mean.<br>
     * 
     * @return True if the request was made using a confidential mean.<br>
     */
    @Override
    public boolean isConfidential() {
        return getRequest().isSecure();
    }

    /**
     * Sends the response back to the client. Commits the status, headers and
     * optional entity and send them on the network.
     * 
     * @param response
     *            The high-level response.
     */
    @Override
    public void sendResponse(Response response) throws IOException {
        // Set the status code in the response. We do this after adding the
        // headers because when we have to rely on the 'sendError' method,
        // the Servlet containers are expected to commit their response.
        if (Status.isError(getStatusCode()) && (response.getEntity() == null)) {
            try {
                // Add the response headers
                Parameter header;
                for (final Iterator<Parameter> iter = getResponseHeaders()
                        .iterator(); iter.hasNext();) {
                    header = iter.next();

                    // We don't need to set the content length, especially
                    // because it could send the response too early on some
                    // containers (ex: Tomcat 5.0).
                    if (!header.getName().equals(
                            HttpConstants.HEADER_CONTENT_LENGTH)) {
                        getResponse().addHeader(header.getName(),
                                header.getValue());
                    }
                }

                getResponse().sendError(getStatusCode(), getReasonPhrase());
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING,
                        "Unable to set the response error status", ioe);
            }
        } else {
            // Send the response entity
            getResponse().setStatus(getStatusCode());

            // Add the response headers after setting the status because
            // otherwise some containers (ex: Tomcat 5.0) immediately send
            // the response if a "Content-Length: 0" header is found.
            Parameter header;
            Parameter contentLengthHeader = null;

            for (final Iterator<Parameter> iter = getResponseHeaders()
                    .iterator(); iter.hasNext();) {
                header = iter.next();

                if (header.getName()
                        .equals(HttpConstants.HEADER_CONTENT_LENGTH)) {
                    contentLengthHeader = header;
                } else {
                    getResponse()
                            .addHeader(header.getName(), header.getValue());
                }
            }

            if (contentLengthHeader != null) {
                getResponse().addHeader(contentLengthHeader.getName(),
                        contentLengthHeader.getValue());
            }

            super.sendResponse(response);
        }
    }

}
