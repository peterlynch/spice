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

package com.noelios.restlet.ext.xdb;

import java.sql.Connection;
import java.util.List;

import javax.servlet.ServletConfig;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.ClientHelper;

/**
 * Connector acting as a WAR client for a Servlet Application. It internally
 * uses one of the available connectors registered with the current Restlet
 * implementation.<br>
 * <br>
 * Here is an example of WAR URI that can be resolved by this client:
 * "war:///WEB-INF/web.xml" If XdbServerServlet is running with SCOTT's
 * credentials and register with a Servlet Name HelloRestlet a WAR URI will be
 * translated to an XMLDB directory:
 * /home/SCOTT/wars/HelloRestlet/WEB-INF/web.xml
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Marcelo F. Ochoa (mochoa@ieee.org)
 */
public class XdbServletWarClient extends Client {
    /** The helper provided by the implementation. */
    private volatile ClientHelper helper;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param config
     *            The Servlet config object.
     * @param conn
     *            The JDBC Connection to XMLDB repository.
     */
    public XdbServletWarClient(Context context, ServletConfig config,
            Connection conn) {
        super(context, (List<Protocol>) null);
        getProtocols().add(Protocol.WAR);
        this.helper = new XdbServletWarClientHelper(this, config, conn);
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private ClientHelper getHelper() {
        return this.helper;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        getHelper().handle(request, response);
    }

    @Override
    public void start() throws Exception {
        super.start();
        getHelper().start();
    }

    @Override
    public void stop() throws Exception {
        getHelper().stop();
        super.stop();
    }

}
