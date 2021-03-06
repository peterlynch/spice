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

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Method;
import org.restlet.data.Range;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.service.RangeService;

/**
 * Filter that is in charge to check the responses to requests for partial
 * content.
 * 
 * @author Thierry Boileau
 */
public class RangeFilter extends Filter {

    /**
     * Constructor.
     * 
     * @param context
     *            The parent context.
     */
    public RangeFilter(Context context) {
        super(context);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (getRangeService().isEnabled()) {
            response.getServerInfo().setAcceptRanges(true);

            if ((Method.GET.equals(request.getMethod()) || Method.HEAD
                    .equals(request.getMethod()))
                    && response.isEntityAvailable()) {
                boolean rangedEntity = response.getEntity().getRange() != null;

                if (response.getStatus().isSuccess()
                        && !Status.SUCCESS_PARTIAL_CONTENT.equals(response
                                .getStatus())) {
                    if (request.getRanges().size() == 1) {
                        // At this time, list of ranges are not supported.
                        Range requestedRange = request.getRanges().get(0);

                        if (response.getEntity().getSize() == Representation.UNKNOWN_SIZE) {
                            if ((requestedRange.getIndex() == Range.INDEX_LAST || requestedRange
                                    .getSize() == Range.SIZE_MAX)
                                    && !(requestedRange.getIndex() == Range.INDEX_LAST && requestedRange
                                            .getSize() == Range.SIZE_MAX)) {
                                // The end index cannot be properly computed
                                response
                                        .setStatus(Status.SERVER_ERROR_INTERNAL);
                                getLogger()
                                        .warning(
                                                "Unable to serve this range since at least the end index of the range cannot be computed.");
                                response.setEntity(null);
                            }
                        } else {

                            if (!requestedRange.equals(response.getEntity()
                                    .getRange())) {
                                if (rangedEntity) {
                                    getLogger()
                                            .info(
                                                    "The range of the response entity is not equal to the requested one.");
                                }

                                response.setEntity(new RangeRepresentation(
                                        response.getEntity(), requestedRange));
                                response
                                        .setStatus(Status.SUCCESS_PARTIAL_CONTENT);
                            }

                        }
                    } else if (request.getRanges().size() > 1) {
                        // Return a server error as this feature isn't supported
                        // yet
                        response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
                        getLogger()
                                .warning(
                                        "Multiple ranges are not supported at this time.");
                        response.setEntity(null);
                    }
                } else {
                    if (rangedEntity) {
                        getLogger()
                                .info(
                                        "The status of a response to a partial GET must be \"206 Partial content\".");
                    }
                }
            }
        }
    }

    /**
     * Returns the Range service of the parent application.
     * 
     * @return The Range service of the parent application.
     */
    public RangeService getRangeService() {
        return getApplication().getRangeService();
    }

}
