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

package com.noelios.restlet.application;

import org.restlet.Application;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

import com.noelios.restlet.StatusFilter;

/**
 * Status filter that tries to obtain ouput representation from an application.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class ApplicationStatusFilter extends StatusFilter {
    /**
     * Constructor.
     * 
     * @param application
     *            The application.
     */
    public ApplicationStatusFilter(Application application) {
        super(application.getContext(), application.getStatusService()
                .isOverwrite(), application.getStatusService()
                .getContactEmail(), (application.getStatusService()
                .getHomeRef().isRelative()) ? application.getStatusService()
                .getHomeRef().toString() : application.getStatusService()
                .getHomeRef().getTargetRef().toString());
    }

    @Override
    public Representation getRepresentation(Status status, Request request,
            Response response) {
        Representation result = getApplication().getStatusService()
                .getRepresentation(status, request, response);
        if (result == null) {
            result = super.getRepresentation(status, request, response);
        }
        return result;
    }

    @Override
    public Status getStatus(Throwable throwable, Request request,
            Response response) {
        Status result = getApplication().getStatusService().getStatus(
                throwable, request, response);
        if (result == null) {
            result = super.getStatus(throwable, request, response);
        }
        return result;
    }

}
