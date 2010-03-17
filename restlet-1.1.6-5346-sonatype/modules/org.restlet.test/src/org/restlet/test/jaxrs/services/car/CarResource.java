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
package org.restlet.test.jaxrs.services.car;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * This class will be returned of {@link CarListResource#findCar(int)}.
 * 
 * @author Stephan Koops
 * @see CarListResource
 * @see EngineResource
 */
public class CarResource {
    /**
     * 
     * @param carNumber
     * @return
     */
    public static String createTextRepr(Object carNumber) {
        return "This is the car with id " + carNumber + ".";
    }

    private final int id;

    /**
     * 
     * @param id
     */
    public CarResource(int id) {
        this.id = id;
    }

    /**
     * @param id
     * @return
     */
    @Path("engine")
    public EngineResource findEngine() {
        return new EngineResource(this);
    }

    /**
     * 
     * @return
     */
    public int getId() {
        return this.id;
    }

    /**
     * 
     * @return
     */
    @GET
    @Produces("text/plain")
    public String getText() {
        return createTextRepr(this.id);
    }
}
