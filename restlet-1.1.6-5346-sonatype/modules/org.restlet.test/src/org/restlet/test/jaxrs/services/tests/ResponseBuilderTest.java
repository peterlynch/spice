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
package org.restlet.test.jaxrs.services.tests;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.data.Dimension;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.ResponseBuilderService;

/**
 * @author Stephan Koops
 * @see ResponseBuilderService
 * @see javax.ws.rs.core.Response.ResponseBuilder
 */
public class ResponseBuilderTest extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(ResponseBuilderService.class);
            }
        };
        return appConfig;
    }

    public void test1() {
        final Response response = get("1");
        final Set<Dimension> dimensions = response.getDimensions();

        assertTrue("dimension must contain MediaType", dimensions
                .contains(Dimension.MEDIA_TYPE));
        assertTrue("dimension must contain Encoding", dimensions
                .contains(Dimension.ENCODING));
    }

    public void test2() {
        final Response response = get("2");
        final Set<Dimension> dimensions = response.getDimensions();

        assertTrue("dimension must contain Language", dimensions
                .contains(Dimension.LANGUAGE));
        assertTrue("dimension must contain CharacterSet", dimensions
                .contains(Dimension.CHARACTER_SET));
    }

    public void testDelete() {
        final Response r = accessServer(Method.DELETE);

        assertEquals(Status.SUCCESS_OK, r.getStatus());
    }
}