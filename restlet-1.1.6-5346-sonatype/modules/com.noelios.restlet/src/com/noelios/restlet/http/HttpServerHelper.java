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

package com.noelios.restlet.http;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Server;
import org.restlet.util.Engine;

import com.noelios.restlet.ServerHelper;

/**
 * Base HTTP server connector. Here is the list of parameters that are
 * supported:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>useForwardedForHeader</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Lookup the "X-Forwarded-For" header supported by popular proxies and
 * caches and uses it to populate the Request.getClientAddresses() method
 * result. This information is only safe for intermediary components within your
 * local network. Other addresses could easily be changed by setting a fake
 * header and should not be trusted for serious security checks.</td>
 * </tr>
 * <tr>
 * <td>converter</td>
 * <td>String</td>
 * <td>com.noelios.restlet.http.HttpServerConverter</td>
 * <td>Class name of the converter of low-level HTTP calls into high level
 * requests and responses.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public class HttpServerHelper extends ServerHelper {
    /** The converter from HTTP calls to uniform calls. */
    private volatile HttpServerConverter converter;

    /**
     * Default constructor. Note that many methods assume that a non-null server
     * is set to work properly. You can use the setHelped(Server) method for
     * this purpose or better rely on the other constructor.
     */
    public HttpServerHelper() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpServerHelper(Server server) {
        super(server);
        this.converter = null;
    }

    /**
     * Returns the converter from HTTP calls to uniform calls.
     * 
     * @return the converter from HTTP calls to uniform calls.
     */
    public HttpServerConverter getConverter() {
        if (this.converter == null) {
            try {
                final String converterClass = getHelpedParameters()
                        .getFirstValue("converter",
                                "com.noelios.restlet.http.HttpServerConverter");
                this.converter = (HttpServerConverter) Engine.loadClass(
                        converterClass).getConstructor(Context.class)
                        .newInstance(getContext());
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server converter", e);
            } catch (SecurityException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server converter", e);
            } catch (InstantiationException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server converter", e);
            } catch (IllegalAccessException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server converter", e);
            } catch (InvocationTargetException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server converter", e);
            } catch (NoSuchMethodException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server converter", e);
            } catch (ClassNotFoundException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server converter", e);
            }
        }

        return this.converter;
    }

    /**
     * Handles the connector call.<br>
     * The default behavior is to create an REST call and delegate it to the
     * attached Restlet.
     * 
     * @param httpCall
     *            The HTTP server call.
     */
    public void handle(HttpServerCall httpCall) {
        try {
            final HttpRequest request = getConverter().toRequest(httpCall);
            final HttpResponse response = new HttpResponse(httpCall, request);
            handle(request, response);
            getConverter().commit(response);
        } catch (Exception e) {
            if ( getLogger().isLoggable( Level.FINE ) ) {
                getLogger().log(Level.FINE,
                                "Error while handling an HTTP server call: ",
                                e );
            }
            else {
                getLogger().log(Level.WARNING,
                                "Error while handling an HTTP server call: " + e.getMessage());
            }
        }
    }

    /**
     * Sets the converter from HTTP calls to uniform calls.
     * 
     * @param converter
     *            The converter to set.
     */
    public void setConverter(HttpServerConverter converter) {
        this.converter = converter;
    }
}
