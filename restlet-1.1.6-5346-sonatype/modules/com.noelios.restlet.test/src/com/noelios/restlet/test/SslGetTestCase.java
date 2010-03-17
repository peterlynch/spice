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

package com.noelios.restlet.test;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Test that a simple get using SSL works for all the connectors.
 * 
 * @author Kevin Conaway
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 */
public class SslGetTestCase extends SslBaseConnectorsTestCase {
    public static class GetTestResource extends Resource {

        public GetTestResource(Context ctx, Request request, Response response) {
            super(ctx, request, response);
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }

        @Override
        public Representation represent(Variant variant) {
            return new StringRepresentation("Hello world", MediaType.TEXT_PLAIN);
        }
    }

    @Override
    protected void call(String uri) throws Exception {
        final Request request = new Request(Method.GET, uri);
        final Client client = new Client(Protocol.HTTPS);
        final Response r = client.handle(request);

        assertEquals(r.getStatus().getDescription(), Status.SUCCESS_OK, r
                .getStatus());
        assertEquals("Hello world", r.getEntity().getText());

        Thread.sleep(200);
        client.stop();
    }

    @Override
    protected Application createApplication(Component component) {
        final Application application = new Application() {
            @Override
            public Restlet createRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", GetTestResource.class);
                return router;
            }
        };

        return application;
    }
}
