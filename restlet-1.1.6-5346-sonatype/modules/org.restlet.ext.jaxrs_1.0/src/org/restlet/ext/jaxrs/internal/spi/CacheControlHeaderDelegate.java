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
package org.restlet.ext.jaxrs.internal.spi;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;

/**
 * {@link HeaderDelegate} for {@link CacheControl}.
 * 
 * @author Stephan Koops
 */
public class CacheControlHeaderDelegate implements HeaderDelegate<CacheControl> {

    /**
     * Obtain an instance of a HeaderDelegate for the CacheControl class.
     * 
     * @see RuntimeDelegate#createHeaderDelegate(Class)
     */
    CacheControlHeaderDelegate() {
    }

    /**
     * Parse the supplied value and create an instance of <code>T</code>.
     * 
     * @param value
     *            the string value
     * @return the newly created instance of <code>T</code>
     * @throws IllegalArgumentException
     *             if the supplied string cannot be parsed
     * @see javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate#fromString(java.lang.String)
     */
    public CacheControl fromString(String value)
            throws IllegalArgumentException {
        // TODO CacheControlHeaderDelegate.fromString(String)
        // illegalargexc
        throw new NotYetImplementedException();
    }

    /**
     * Convert the supplied value to a String.
     * 
     * @param value
     *            the value of type <code>T</code>
     * @return a String representation of the value
     * @throws IllegalArgumentException
     *             if the supplied object cannot be serialized
     * @see javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate#toString(java.lang.Object)
     */
    public String toString(CacheControl cacheControl) {
        // TODO CacheControlHeaderDelegate.toString(CacheControl)
        // illegalargexc
        throw new NotYetImplementedException();
    }
}
