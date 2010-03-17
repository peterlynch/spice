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

package com.noelios.restlet.component;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Router that collects calls from all applications and dispatches them to the
 * appropriate client connectors.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class ClientRouter extends Router {
    /** The parent component. */
    private volatile Component component;

    /**
     * Constructor.
     * 
     * @param component
     *            The parent component.
     */
    public ClientRouter(Component component) {
        super((component == null) ? null : component.getContext()
                .createChildContext());
        this.component = component;
    }

    @Override
    public Restlet getNext(Request request, Response response) {
        Restlet result = super.getNext(request, response);
        if (result == null) {
            getLogger()
                    .warning(
                            "The protocol used by this request is not declared in the list of client connectors. ("
                                    + request.getResourceRef().getSchemeProtocol()
                                    + ")");
        }
        return result;
    }

    /**
     * Returns the parent component.
     * 
     * @return The parent component.
     */
    private Component getComponent() {
        return this.component;
    }

    /** Starts the Restlet. */
    @Override
    public synchronized void start() throws Exception {
        for (final Client client : getComponent().getClients()) {
            getRoutes().add(new ClientRoute(this, client));
        }

        super.start();
    }
}
