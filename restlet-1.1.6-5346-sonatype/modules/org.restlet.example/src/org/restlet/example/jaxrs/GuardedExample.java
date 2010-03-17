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
package org.restlet.example.jaxrs;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import org.restlet.Component;
import org.restlet.Guard;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.restlet.ext.jaxrs.RoleChecker;

/**
 * <p>
 * This class shows how to use the Restlet JAX-RS extension with access control.
 * </p>
 * <p>
 * Start this class, open a browser and click <a
 * href="http://localhost/easy">easy</a> or <a
 * href="http://localhost/persons">persons</a> with one of the following user /
 * password combinations:
 * <ul>
 * <li>admin / adminPW</li>
 * <li>alice / alicesSecret</li>
 * <li>bob / bobsSecret</li>
 * </ul>
 * </p>
 * 
 * @author Stephan Koops
 * @see ExampleServer
 * @see ExampleApplication
 */
public class GuardedExample {

    /**
     * An example {@link RoleChecker}. This example allows anything to user
     * admin and only read to any other user. <br>
     * This RoleChecker isn't used by the resources.
     * 
     * @author Stephan Koops
     */
    private static final class ExampleRoleChecker implements RoleChecker {
        /**
         * @see RoleChecker#isInRole(Principal, String)
         * @see SecurityContext#isUserInRole(String)
         */
        public boolean isInRole(Principal principal, String role) {
            // access database or whatever
            // example: user "admin" has all roles
            if (principal.getName().equalsIgnoreCase("admin")) {
                return true;
            }
            // example: every authenticatd user could read
            if (role.equalsIgnoreCase("read")) {
                return true;
            }
            // the normal users have no other roles.
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        // create Component (as ever for Restlet)
        final Component comp = new Component();
        final Server server = comp.getServers().add(Protocol.HTTP, 80);

        // create JAX-RS runtime environment
        final JaxRsApplication application = new JaxRsApplication(comp
                .getContext().createChildContext());

        // create a Guard
        final Guard guard = new Guard(application.getContext(),
                ChallengeScheme.HTTP_BASIC, "JAX-RS example");
        // set valid users and thier passwords.
        guard.getSecrets().put("admin", "adminPW".toCharArray());
        guard.getSecrets().put("alice", "alicesSecret".toCharArray());
        guard.getSecrets().put("bob", "bobsSecret".toCharArray());

        // create an RoleChecker (see above)
        final ExampleRoleChecker roleChecker = new ExampleRoleChecker();
        // attach Guard and RoleChecker
        application.setAuthentication(guard, roleChecker);

        // attach ApplicationConfig
        application.add(new ExampleApplication());

        // Attach the application to the component and start it
        comp.getDefaultHost().attach(application);
        comp.start();

        System.out.println("Server started on port " + server.getPort());
        System.out.println("Press key to stop server");
        System.in.read();
        System.out.println("Stopping server");
        comp.stop();
        System.out.println("Server stopped");
    }
}