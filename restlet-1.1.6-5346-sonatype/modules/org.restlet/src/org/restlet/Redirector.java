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

package org.restlet;

import java.util.logging.Level;

import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.util.Template;

/**
 * Rewrites URIs then redirects the call or the client to a new destination.
 * There are various redirection modes that you can choose from: client-side
 * redirections ({@link #MODE_CLIENT_FOUND}, {@link #MODE_CLIENT_PERMANENT},
 * {@link #MODE_CLIENT_SEE_OTHER}, {@link #MODE_CLIENT_TEMPORARY}) or
 * server-side redirections, similar to a reverse proxy (
 * {@link #MODE_DISPATCHER}).<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see org.restlet.util.Template
 * @see <a
 *      href="http://www.restlet.org/documentation/1.1/tutorial#part10">Tutorial:
 *      URI rewriting and redirection</a>
 * @author Jerome Louvel
 */
public class Redirector extends Restlet {
    /**
     * In this mode, the client is permanently redirected to the URI generated
     * from the target URI pattern.<br>
     * See org.restlet.data.Status.REDIRECTION_PERMANENT.
     */
    public static final int MODE_CLIENT_PERMANENT = 1;

    /**
     * In this mode, the client is simply redirected to the URI generated from
     * the target URI pattern.<br>
     * See org.restlet.data.Status.REDIRECTION_FOUND.
     */
    public static final int MODE_CLIENT_FOUND = 2;

    /**
     * In this mode, the client is simply redirected to the URI generated from
     * the target URI pattern.<br>
     * See org.restlet.data.Status.REDIRECTION_SEE_OTHER.
     */
    public static final int MODE_CLIENT_SEE_OTHER = 3;

    /**
     * In this mode, the client is temporarily redirected to the URI generated
     * from the target URI pattern.<br>
     * See org.restlet.data.Status.REDIRECTION_TEMPORARY.
     */
    public static final int MODE_CLIENT_TEMPORARY = 4;

    /**
     * In this mode, the call is sent to the context's dispatcher. Once the
     * selected client connector has completed the request handling, the
     * response is normally returned to the client. In this case, you can view
     * the Redirector as acting as a transparent proxy Restlet.<br>
     * <br>
     * Remember to add the required connectors to the parent Component and to
     * declare them in the list of required connectors on the
     * Application.connectorService property.<br>
     * <br>
     * Note that in this mode, the headers of HTTP requests, stored in the
     * request's attributes, are removed before dispatching. Also, when a HTTP
     * response comes back the headers are also removed.
     */
    public static final int MODE_DISPATCHER = 5;

    /** The target URI pattern. */
    protected volatile String targetTemplate;

    /** The redirection mode. */
    protected volatile int mode;

    /**
     * Constructor for the dispatcher mode.
     * 
     * @param context
     *            The context.
     * @param targetTemplate
     *            The template to build the target URI.
     * @see org.restlet.util.Template
     */
    public Redirector(Context context, String targetTemplate) {
        this(context, targetTemplate, MODE_DISPATCHER);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param targetPattern
     *            The pattern to build the target URI (using StringTemplate
     *            syntax and the CallModel for variables).
     * @param mode
     *            The redirection mode.
     */
    public Redirector(Context context, String targetPattern, int mode) {
        super(context);
        this.targetTemplate = targetPattern;
        this.mode = mode;
    }

    /**
     * Returns the redirection mode.
     * 
     * @return The redirection mode.
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * Returns the target reference to redirect to.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The target reference to redirect to.
     */
    protected Reference getTargetRef(Request request, Response response) {
        // Create the template
        final Template rt = new Template(this.targetTemplate);
        rt.setLogger(getLogger());

        // Return the formatted target URI
        return new Reference(rt.format(request, response));
    }

    /**
     * Returns the target URI pattern.
     * 
     * @return The target URI pattern.
     */
    public String getTargetTemplate() {
        return this.targetTemplate;
    }

    /**
     * Handles a call by redirecting using the selected redirection mode.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        // Generate the target reference
        final Reference targetRef = getTargetRef(request, response);

        switch (this.mode) {
        case MODE_CLIENT_PERMANENT:
            getLogger().log(Level.INFO,
                    "Permanently redirecting client to: " + targetRef);
            response.redirectPermanent(targetRef);
            break;

        case MODE_CLIENT_FOUND:
            getLogger().log(Level.INFO,
                    "Redirecting client to found location: " + targetRef);
            response.setLocationRef(targetRef);
            response.setStatus(Status.REDIRECTION_FOUND);
            break;

        case MODE_CLIENT_SEE_OTHER:
            getLogger().log(Level.INFO,
                    "Redirecting client to another location: " + targetRef);
            response.setLocationRef(targetRef);
            response.setStatus(Status.REDIRECTION_SEE_OTHER);
            break;

        case MODE_CLIENT_TEMPORARY:
            getLogger().log(Level.INFO,
                    "Temporarily redirecting client to: " + targetRef);
            response.redirectTemporary(targetRef);
            break;

        case MODE_DISPATCHER:
            getLogger().log(Level.INFO,
                    "Redirecting via client connector to: " + targetRef);
            redirectDispatcher(targetRef, request, response);
            break;
        }
    }

    /**
     * Redirects a given call to a target reference. In the default
     * implementation, the request HTTP headers, stored in the request's
     * attributes, are removed before dispatching. After dispatching, the
     * response HTTP headers are also removed to prevent conflicts with the main
     * call.
     * 
     * @param targetRef
     *            The target reference with URI variables resolved.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void redirectDispatcher(Reference targetRef, Request request,
            Response response) {
        // Save the base URI if it exists as we might need it for redirections
        final Reference baseRef = request.getResourceRef().getBaseRef();

        // Update the request to cleanly go to the target URI
        request.setResourceRef(targetRef);
        request.getAttributes().remove("org.restlet.http.headers");
        getContext().getClientDispatcher().handle(request, response);

        // Allow for response rewriting and clean the headers
        response.setEntity(rewrite(response.getEntity()));
        response.getAttributes().remove("org.restlet.http.headers");

        // In case of redirection, we may have to rewrite the redirect URI
        if (response.getLocationRef() != null) {
            final Template rt = new Template(this.targetTemplate);
            rt.setLogger(getLogger());
            final int matched = rt.parse(response.getLocationRef().toString(),
                    request);

            if (matched > 0) {
                final String remainingPart = (String) request.getAttributes()
                        .get("rr");

                if (remainingPart != null) {
                    response.setLocationRef(baseRef.toString() + remainingPart);
                }
            }
        }
    }

    /**
     * Optionnaly rewrites the response entity returned in the MODE_CONNECTOR
     * mode. By default, it just returns the initial entity without any
     * modification.
     * 
     * @param initialEntity
     *            The initial entity returned.
     * @return The rewritten entity.
     */
    protected Representation rewrite(Representation initialEntity) {
        return initialEntity;
    }

    /**
     * Sets the redirection mode.
     * 
     * @param mode
     *            The redirection mode.
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Sets the target URI pattern.
     * 
     * @param targetTemplate
     *            The target URI pattern.
     */
    public void setTargetTemplate(String targetTemplate) {
        this.targetTemplate = targetTemplate;
    }

}
