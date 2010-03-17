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

package com.noelios.restlet.ext.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Route;
import org.restlet.Server;
import org.restlet.VirtualHost;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.util.Engine;

import com.noelios.restlet.component.ComponentContext;
import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerHelper;

/**
 * Servlet acting like an HTTP server connector. See <a
 * href="/documentation/1.1/faq#02">Developper FAQ #2</a> for details on how to
 * integrate a Restlet application into a servlet container.<br>
 * <br>
 * Initially designed to deploy a single Restlet Application, this Servlet can
 * now deploy a complete Restlet Component. This allows you to reuse an existing
 * standalone Restlet Component, potentially containing several applications,
 * and declaring client connectors, for example for the CLAP, FILE or HTTP
 * protocols.<br>
 * <br>
 * There are three separate ways to configure the deployment using this Servlet.
 * They are described below by order of priority:
 * <table>
 * <tr>
 * <th>Mode</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><b>1</b></td>
 * <td>If a "/WEB-INF/restlet.xml" file exists and contains a valid XML
 * configuration as described in the documentation of the {@link Component}
 * class. It is used to instantiate and attach the described component,
 * contained applications and connectors.</td>
 * </tr>
 * <tr>
 * <td><b>2</b></td>
 * <td>If the "/WEB-INF/web.xml" file contains a context parameter named
 * "org.restlet.component", its value must be the path of a class that inherits
 * from {@link Component}. It is used to instantiate and attach the described
 * component, contained applications and connectors.</td>
 * </tr>
 * <tr>
 * <td><b>3</b></td>
 * <td>If the "/WEB-INF/web.xml" file contains a context parameter named
 * "org.restlet.application", its value must be the path of a class that
 * inherits from {@link Application}. It is used to instantiate the application
 * and to attach it to a default Restlet Component.</td>
 * </tr>
 * </table>
 * <br>
 * In deployment mode 3, you can also add an optionnal "org.restlet.clients"
 * context parameter that contains a space separated list of client protocols
 * supported by the underlying component. For each one, a new client connector
 * is added to the Component instance.<br>
 * 
 * Here is a template configuration for the ServerServlet:
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;ISO-8859-1&quot;?&gt;
 * &lt;!DOCTYPE web-app PUBLIC &quot;-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN&quot; &quot;http://java.sun.com/dtd/web-app_2_3.dtd&quot;&gt;
 * &lt;web-app&gt;
 *         &lt;display-name&gt;Restlet adapter&lt;/display-name&gt;
 * 
 *         &lt;!-- Your component class name (Optional - For mode 2) --&gt;
 *         &lt;context-param&gt;
 *                 &lt;param-name&gt;org.restlet.component&lt;/param-name&gt;
 *                 &lt;param-value&gt;com.mycompany.MyComponent&lt;/param-value&gt;
 *         &lt;/context-param&gt;
 *         
 *         &lt;!-- Your application class name (Optional - For mode 3) --&gt;
 *         &lt;context-param&gt;
 *                 &lt;param-name&gt;org.restlet.application&lt;/param-name&gt;
 *                 &lt;param-value&gt;com.mycompany.MyApplication&lt;/param-value&gt;
 *         &lt;/context-param&gt;
 *         
 *         &lt;!-- List of supported client protocols (Optional - Only in mode 3) --&gt;
 *         &lt;context-param&gt;
 *                 &lt;param-name&gt;org.restlet.clients&lt;/param-name&gt;
 *                 &lt;param-value&gt;HTTP HTTPS FILE&lt;/param-value&gt;
 *         &lt;/context-param&gt;
 
 *         &lt;!-- Add the Servlet context path to the routes (Optional - true by default) --&gt;
 *         &lt;context-param&gt;
 *                 &lt;param-name&gt;org.restlet.autoWire&lt;/param-name&gt;
 *                 &lt;param-value&gt;true&lt;/param-value&gt;
 *         &lt;/context-param&gt;
 * 
 *         &lt;!-- Restlet adapter (Mandatory) --&gt;
 *         &lt;servlet&gt;
 *                 &lt;servlet-name&gt;ServerServlet&lt;/servlet-name&gt;
 *                 &lt;servlet-class&gt;com.noelios.restlet.ext.servlet.ServerServlet&lt;/servlet-class&gt;
 *         &lt;/servlet&gt;
 * 
 *         &lt;!-- Catch all requests (Mandatory) --&gt;
 *         &lt;servlet-mapping&gt;
 *                 &lt;servlet-name&gt;ServerServlet&lt;/servlet-name&gt;
 *                 &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *         &lt;/servlet-mapping&gt;
 * &lt;/web-app&gt;
 * </pre>
 * 
 * Note that the enumeration of "initParameters" of your Servlet will be copied
 * to the "context.parameters" property of your Restlet Application. This way,
 * you can pass additional initialization parameters to your application, and
 * maybe share them with other Servlets.<br>
 * <br>
 * An additionnal boolean parameter called "org.restlet.autoWire" allows you to
 * control the way your customized Component fits in the context of the wrapping
 * Servlet. The root cause is that both your Servlet Container and your Restlet
 * Component handle part of the URI routing, respectively to the right Servlet
 * and to the right virtual host and Restlets (most of the time Application
 * instances).<br>
 * <br>
 * When a request reaches the Servlet container, it is first routed acccording
 * to its web.xml configuration (i.e. declared virtual hosts and webapp context
 * path which is generally the name of the webapp war file). Once the incoming
 * request reaches the ServerServlet and the wrapped Restlet Component, its URI
 * is, for the second time, entirely subject to a separate routing chain. It
 * begins with the virtual hosts, then continue to the URI pattern used when
 * attaching Restlets to the host. The important conclusion is that both routing
 * configurations must be consistent in order to work fine.<br>
 * <br>
 * In deployment mode 3, the context path of the servlet is automatically added.
 * That's what we call the auto-wire feature. This is the default case, and is
 * equivalent to setting the value "true" for the "org.restlet.autoWire"
 * parameter as described above. In modes 1 or 2, if you want to manually
 * control the URI wiring, you can disable the auto-wiring by setting the
 * property to "false".
 * 
 * @see <a href="http://java.sun.com/j2ee/">J2EE home page</a>
 * @author Jerome Louvel
 */
public class ServerServlet extends HttpServlet {
    /**
     * Name of the attribute key containing a reference to the current
     * application.
     */
    private static final String APPLICATION_KEY = "org.restlet.application";

    /**
     * The Servlet context initialization parameter's name containing a boolean
     * value. "true" indicates that all applications will be attached to the
     * Component's virtual hosts with the Servlet Context path value.
     */
    private static final String AUTO_WIRE_KEY = "org.restlet.autoWire";

    /** The default value for the AUTO_WIRE_KEY parameter. */
    private static final String AUTO_WIRE_KEY_DEFAULT = "true";

    /**
     * Name of the attribute key containing a list of supported client
     * protocols.
     */
    private static final String CLIENTS_KEY = "org.restlet.clients";

    /**
     * Name of the attribute key containing a reference to the current
     * component.
     */
    private static final String COMPONENT_KEY = "org.restlet.component";

    /**
     * The Servlet context initialization parameter's name containing the name
     * of the Servlet context attribute that should be used to store the Restlet
     * Application instance.
     */
    private static final String NAME_APPLICATION_ATTRIBUTE = "org.restlet.attribute.application";

    /** The default value for the NAME_APPLICATION_ATTRIBUTE parameter. */
    private static final String NAME_APPLICATION_ATTRIBUTE_DEFAULT = "com.noelios.restlet.ext.servlet.ServerServlet.application";

    /**
     * The Servlet context initialization parameter's name containing the name
     * of the Servlet context attribute that should be used to store the Restlet
     * Component instance.
     */
    private static final String NAME_COMPONENT_ATTRIBUTE = "org.restlet.attribute.component";

    /** The default value for the NAME_COMPONENT_ATTRIBUTE parameter. */
    private static final String NAME_COMPONENT_ATTRIBUTE_DEFAULT = "com.noelios.restlet.ext.servlet.ServerServlet.component";

    /**
     * The Servlet context initialization parameter's name containing the name
     * of the Servlet context attribute that should be used to store the HTTP
     * server connector instance.
     */
    private static final String NAME_SERVER_ATTRIBUTE = "org.restlet.attribute.server";

    /** The default value for the NAME_SERVER_ATTRIBUTE parameter. */
    private static final String NAME_SERVER_ATTRIBUTE_DEFAULT = "com.noelios.restlet.ext.servlet.ServerServlet.server";

    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** The associated Restlet application. */
    private volatile transient Application application;

    /** The associated Restlet component. */
    private volatile transient Component component;

    /** The associated HTTP server helper. */
    private volatile transient HttpServerHelper helper;

    /**
     * Constructor.
     */
    public ServerServlet() {
        this.application = null;
        this.component = null;
        this.helper = null;
    }

    /**
     * Creates the single Application used by this Servlet.
     * 
     * @param parentContext
     *            The parent component context.
     * 
     * @return The newly created Application or null if unable to create
     */
    @SuppressWarnings("unchecked")
    protected Application createApplication(Context parentContext) {
        Application application = null;

        // Try to instantiate a new target application
        // First, find the application class name
        final String applicationClassName = getInitParameter(APPLICATION_KEY,
                null);

        // Load the application class using the given class name
        if (applicationClassName != null) {
            try {
                final Class<?> targetClass = loadClass(applicationClassName);

                try {
                    // Instantiate an application with the default constructor
                    // then invoke the setContext method.
                    application = (Application) targetClass.getConstructor()
                            .newInstance();

                    // Set the context based on the Servlet's context
                    application.setContext(new ServletContextAdapter(this,
                            parentContext));
                } catch (NoSuchMethodException e) {
                    log("[Noelios Restlet Engine] - The ServerServlet couldn't invoke the constructor of the target class. Please check this class has a constructor without parameter. The constructor with a parameter of type Context will be used instead.");
                    // The constructor with the Context parameter does not
                    // exist. Create a new instance of the application class by
                    // invoking the constructor with the Context parameter.
                    application = (Application) targetClass.getConstructor(
                            Context.class).newInstance(
                            new ServletContextAdapter(this, parentContext));
                }
            } catch (ClassNotFoundException e) {
                log(
                        "[Noelios Restlet Engine] - The ServerServlet couldn't find the target class. Please check that your classpath includes "
                                + applicationClassName, e);

            } catch (InstantiationException e) {
                log(
                        "[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. Please check this class has an empty constructor "
                                + applicationClassName, e);
            } catch (IllegalAccessException e) {
                log(
                        "[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. Please check that you have to proper access rights to "
                                + applicationClassName, e);
            } catch (NoSuchMethodException e) {
                log(
                        "[Noelios Restlet Engine] - The ServerServlet couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of Context "
                                + applicationClassName, e);
            } catch (InvocationTargetException e) {
                log(
                        "[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. An exception was thrown while creating "
                                + applicationClassName, e);
            }
        }

        if (application != null) {
            final Context applicationContext = application.getContext();

            // Copy all the servlet parameters into the context
            String initParam;

            // Copy all the Servlet component initialization parameters
            final javax.servlet.ServletConfig servletConfig = getServletConfig();
            for (final Enumeration<String> enum1 = servletConfig
                    .getInitParameterNames(); enum1.hasMoreElements();) {
                initParam = enum1.nextElement();
                applicationContext.getParameters().add(initParam,
                        servletConfig.getInitParameter(initParam));
            }

            // Copy all the Servlet application initialization parameters
            for (final Enumeration<String> enum1 = getServletContext()
                    .getInitParameterNames(); enum1.hasMoreElements();) {
                initParam = enum1.nextElement();
                applicationContext.getParameters().add(initParam,
                        getServletContext().getInitParameter(initParam));
            }
        }

        return application;
    }

    /**
     * Creates a new Servlet call wrapping a Servlet request/response couple and
     * a Server connector.
     * 
     * @param server
     *            The Server connector.
     * @param request
     *            The Servlet request.
     * @param response
     *            The Servlet response.
     * @return The new ServletCall instance.
     */
    protected HttpServerCall createCall(Server server,
            HttpServletRequest request, HttpServletResponse response) {
        return new ServletCall(server, request, response);
    }

    /**
     * Creates the single Component used by this Servlet.
     * 
     * @return The newly created Component or null if unable to create.
     */
    @SuppressWarnings("unchecked")
    protected Component createComponent() {
        Component component = null;

        // Look for the Component XML configuration file.
        Client client = createWarClient(new Context(), getServletConfig());
        Response response = client.get("war:///WEB-INF/restlet.xml");
        if (response.getStatus().isSuccess() && response.isEntityAvailable()) {
            component = new Component(response.getEntity());
        }

        // Look for the component class name specified in the web.xml file.
        if (component == null) {
            // Try to instantiate a new target component
            // First, find the component class name
            final String componentClassName = getInitParameter(COMPONENT_KEY,
                    null);

            // Load the component class using the given class name
            if (componentClassName != null) {
                try {
                    final Class<?> targetClass = loadClass(componentClassName);

                    // Create a new instance of the component class by
                    // invoking the constructor with the Context parameter.
                    component = (Component) targetClass.newInstance();
                } catch (ClassNotFoundException e) {
                    log(
                            "[Noelios Restlet Engine] - The ServerServlet couldn't find the target class. Please check that your classpath includes "
                                    + componentClassName, e);
                } catch (InstantiationException e) {
                    log(
                            "[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. Please check this class has an empty constructor "
                                    + componentClassName, e);
                } catch (IllegalAccessException e) {
                    log(
                            "[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. Please check that you have to proper access rights to "
                                    + componentClassName, e);
                }
            }
        }

        // Create the default Component
        if (component == null) {
            component = new Component();

            // The status service is disabled by default.
            component.getStatusService().setEnabled(false);

            // Define the list of supported client protocols.
            final String clientProtocolsString = getInitParameter(CLIENTS_KEY,
                    null);
            if (clientProtocolsString != null) {
                final String[] clientProtocols = clientProtocolsString
                        .split(" ");
                for (final String clientProtocol : clientProtocols) {
                    component.getClients()
                            .add(Protocol.valueOf(clientProtocol));
                }
            }
        }

        // Complete the configuration of the Component
        // Add the WAR client
        component.getClients().add(
                createWarClient(component.getContext(), getServletConfig()));

        // Copy all the servlet parameters into the context
        final ComponentContext componentContext = (ComponentContext) component
                .getContext();
        String initParam;

        // Copy all the Servlet container initialization parameters
        final javax.servlet.ServletConfig servletConfig = getServletConfig();
        for (final Enumeration<String> enum1 = servletConfig
                .getInitParameterNames(); enum1.hasMoreElements();) {
            initParam = enum1.nextElement();
            componentContext.getParameters().add(initParam,
                    servletConfig.getInitParameter(initParam));
        }

        // Copy all the Servlet application initialization parameters
        for (final Enumeration<String> enum1 = getServletContext()
                .getInitParameterNames(); enum1.hasMoreElements();) {
            initParam = enum1.nextElement();
            componentContext.getParameters().add(initParam,
                    getServletContext().getInitParameter(initParam));
        }

        // Copy all Servlet's context attributes
        String attributeName;
        for (final Enumeration<String> namesEnum = getServletContext()
                .getAttributeNames(); namesEnum.hasMoreElements();) {
            attributeName = namesEnum.nextElement();
            componentContext.getAttributes().put(attributeName,
                    getServletContext().getAttribute(attributeName));
        }

        return component;
    }

    /**
     * Creates the associated HTTP server handling calls.
     * 
     * @param request
     *            The HTTP Servlet request.
     * @return The new HTTP server handling calls.
     */
    protected HttpServerHelper createServer(HttpServletRequest request) {
        HttpServerHelper result = null;
        final Component component = getComponent();

        if (component != null) {
            // First, let's create a pseudo server
            final Server server = new Server(component.getContext()
                    .createChildContext(), (List<Protocol>) null, request
                    .getLocalAddr(), request.getLocalPort(), component);
            result = new HttpServerHelper(server);

            // Attach the hosted application(s) to the right path
            final String uriPattern = request.getContextPath()
                    + request.getServletPath();

            if (isDefaultComponent()) {
                if (this.application != null) {
                    log("[Noelios Restlet Engine] - Attaching application: "
                            + this.application + " to URI: " + uriPattern);
                    component.getDefaultHost().attach(uriPattern,
                            this.application);
                }
            } else {
                // According to the mode, configure correctly the component.
                final String autoWire = getInitParameter(AUTO_WIRE_KEY,
                        AUTO_WIRE_KEY_DEFAULT);
                if (AUTO_WIRE_KEY_DEFAULT.equalsIgnoreCase(autoWire)) {
                    // Translate all defined routes as much as possible
                    // with the context path only or the full servlet path.

                    // 1- get the offset
                    boolean addContextPath = false;
                    boolean addFullServletPath = false;

                    if (component.getDefaultHost().getRoutes().isEmpty()) {
                        // Case where the default host has a default route (with
                        // an empty pattern).
                        addFullServletPath = component.getDefaultHost()
                                .getDefaultRoute() != null;
                    } else {
                        for (final Route route : component.getDefaultHost()
                                .getRoutes()) {
                            if (route.getTemplate().getPattern() == null) {
                                addFullServletPath = true;
                                continue;
                            }

                            if (!route.getTemplate().getPattern().startsWith(
                                    uriPattern)) {
                                if (!route.getTemplate().getPattern()
                                        .startsWith(request.getServletPath())) {
                                    addFullServletPath = true;
                                } else {
                                    addContextPath = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!addContextPath) {
                        for (final VirtualHost virtualHost : component
                                .getHosts()) {
                            if (virtualHost.getRoutes().isEmpty()) {
                                // Case where the default host has a default
                                // route (with an empty pattern).
                                addFullServletPath = virtualHost
                                        .getDefaultRoute() != null;
                            } else {
                                for (final Route route : virtualHost
                                        .getRoutes()) {
                                    if (route.getTemplate().getPattern() == null) {
                                        addFullServletPath = true;
                                        continue;
                                    }

                                    if (!route.getTemplate().getPattern()
                                            .startsWith(uriPattern)) {
                                        if (!route
                                                .getTemplate()
                                                .getPattern()
                                                .startsWith(
                                                        request
                                                                .getServletPath())) {
                                            addFullServletPath = true;
                                        } else {
                                            addContextPath = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (addContextPath) {
                                break;
                            }
                        }
                    }

                    // 2- Translate all routes.
                    if (addContextPath || addFullServletPath) {
                        String offsetPath = null;

                        if (addContextPath) {
                            offsetPath = request.getContextPath();
                        } else {
                            offsetPath = uriPattern;
                        }

                        // Shift the default route (if any) of the default host
                        Route defaultRoute = component.getDefaultHost()
                                .getDefaultRoute();
                        if (defaultRoute != null) {
                            defaultRoute.getTemplate().setPattern(
                                    offsetPath
                                            + defaultRoute.getTemplate()
                                                    .getPattern());
                            log("[Noelios Restlet Engine] - Attaching restlet: "
                                    + defaultRoute.getNext()
                                    + " to URI: "
                                    + offsetPath
                                    + defaultRoute.getTemplate().getPattern());
                        }

                        // Shift the routes of the default host
                        for (final Route route : component.getDefaultHost()
                                .getRoutes()) {
                            log("[Noelios Restlet Engine] - Attaching restlet: "
                                    + route.getNext()
                                    + " to URI: "
                                    + offsetPath
                                    + route.getTemplate().getPattern());
                            route.getTemplate().setPattern(
                                    offsetPath
                                            + route.getTemplate().getPattern());
                        }
                        for (final VirtualHost virtualHost : component
                                .getHosts()) {
                            // Shift the default route (if any) of the virtual
                            // host
                            defaultRoute = virtualHost.getDefaultRoute();
                            if (defaultRoute != null) {
                                defaultRoute.getTemplate().setPattern(
                                        offsetPath
                                                + defaultRoute.getTemplate()
                                                        .getPattern());
                                log("[Noelios Restlet Engine] - Attaching restlet: "
                                        + defaultRoute.getNext()
                                        + " to URI: "
                                        + offsetPath
                                        + defaultRoute.getTemplate()
                                                .getPattern());
                            }
                            // Shift the routes of the virtual host
                            for (final Route route : virtualHost.getRoutes()) {
                                log("[Noelios Restlet Engine] - Attaching restlet: "
                                        + route.getNext()
                                        + " to URI: "
                                        + offsetPath
                                        + route.getTemplate().getPattern());
                                route.getTemplate().setPattern(
                                        offsetPath
                                                + route.getTemplate()
                                                        .getPattern());
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Creates a new client for the WAR protocol.
     * 
     * @param context
     *            The parent context.
     * @param config
     *            The Servlet config.
     * @return The new WAR client instance.
     */
    protected Client createWarClient(Context context, ServletConfig config) {
        return new ServletWarClient(context, config.getServletContext());
    }

    @Override
    public void destroy() {
        if ((getComponent() != null) && (getComponent().isStarted())) {
            try {
                getComponent().stop();
            } catch (Exception e) {
                log("Error during the stopping of the Restlet Component", e);
            }
        }

        super.destroy();
    }

    /**
     * Returns the application. It creates a new one if none exists.
     * 
     * @return The application.
     */
    public Application getApplication() {
        Application result = this.application;

        if (result == null) {
            synchronized (ServerServlet.class) {
                if (result == null) {
                    // In case a component is explicitely defined, it cannot be
                    // completed.
                    if (isDefaultComponent()) {
                        // Find the attribute name to use to store the
                        // application
                        final String applicationAttributeName = getInitParameter(
                                NAME_APPLICATION_ATTRIBUTE,
                                NAME_APPLICATION_ATTRIBUTE_DEFAULT);

                        // Look up the attribute for a target
                        result = (Application) getServletContext()
                                .getAttribute(applicationAttributeName);

                        if (result == null) {
                            result = createApplication(getComponent()
                                    .getContext());
                            getServletContext().setAttribute(
                                    applicationAttributeName, result);
                        }

                        this.application = result;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the component. It creates a new one if none exists.
     * 
     * @return The component.
     */
    public Component getComponent() {
        Component result = this.component;

        if (result == null) {
            synchronized (ServerServlet.class) {
                if (result == null) {
                    // Find the attribute name to use to store the component
                    final String componentAttributeName = getInitParameter(
                            NAME_COMPONENT_ATTRIBUTE,
                            NAME_COMPONENT_ATTRIBUTE_DEFAULT);

                    // Look up the attribute for a target
                    result = (Component) getServletContext().getAttribute(
                            componentAttributeName);

                    if (result == null) {
                        result = createComponent();
                        getServletContext().setAttribute(
                                componentAttributeName, result);
                    }
                }

                this.component = result;
            }
        }

        return result;
    }

    /**
     * Returns the value of a given initialization parameter, first from the
     * Servlet configuration, then from the Web Application context.
     * 
     * @param name
     *            The parameter name.
     * @param defaultValue
     *            The default to use in case the parameter is not found.
     * @return The value of the parameter or null.
     */
    public String getInitParameter(String name, String defaultValue) {
        String result = getServletConfig().getInitParameter(name);

        if (result == null) {
            result = getServletConfig().getServletContext().getInitParameter(
                    name);
        }

        if (result == null) {
            result = defaultValue;
        }

        return result;
    }

    /**
     * Returns the associated HTTP server handling calls. It creates a new one
     * if none exists.
     * 
     * @param request
     *            The HTTP Servlet request.
     * @return The HTTP server handling calls.
     */
    public HttpServerHelper getServer(HttpServletRequest request) {
        HttpServerHelper result = this.helper;

        if (result == null) {
            synchronized (ServerServlet.class) {
                if (result == null) {
                    // Find the attribute name to use to store the server
                    // reference
                    final String serverAttributeName = getInitParameter(
                            NAME_SERVER_ATTRIBUTE,
                            NAME_SERVER_ATTRIBUTE_DEFAULT);

                    // Look up the attribute for a target
                    result = (HttpServerHelper) getServletContext()
                            .getAttribute(serverAttributeName);

                    if (result == null) {
                        result = createServer(request);
                        getServletContext().setAttribute(serverAttributeName,
                                result);
                    }

                    this.helper = result;
                }
            }
        }

        return result;
    }

    @Override
    public void init() throws ServletException {
        if ((getComponent() != null)) {
            if ((getApplication() != null) && (getApplication().isStopped())) {
                try {
                    getApplication().start();
                } catch (Exception e) {
                    log("Error during the starting of the Restlet Application",
                            e);
                }
            }
        }
    }

    /**
     * Indicates if the Component hosted by this Servlet is the default one or
     * one provided by the user.
     * 
     * @return True if the Component is the default one, false otherwise.
     */
    private boolean isDefaultComponent() {
        // The Component is provided via an XML configuration file.
        Client client = createWarClient(new Context(), getServletConfig());
        Response response = client.get("war:///WEB-INF/restlet.xml");
        if (response.getStatus().isSuccess() && response.isEntityAvailable()) {
            return false;
        }

        // The Component is provided via a context parameter in the "web.xml"
        // file.
        final String componentAttributeName = getInitParameter(COMPONENT_KEY,
                null);
        if (componentAttributeName != null) {
            return false;
        }

        return true;
    }

    /**
     * Returns a class for a given qualified class name.
     * 
     * @param className
     *            The class name to lookup.
     * @return The class object.
     * @throws ClassNotFoundException
     */
    protected Class<?> loadClass(String className)
            throws ClassNotFoundException {
        return Engine.loadClass(className);
    }

    /**
     * Services a HTTP Servlet request as an uniform call.
     * 
     * @param request
     *            The HTTP Servlet request.
     * @param response
     *            The HTTP Servlet response.
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final HttpServerHelper helper = getServer(request);

        if (helper != null) {
            helper.handle(createCall(helper.getHelped(), request, response));
        } else {
            log("[Noelios Restlet Engine] - Unable to get the Restlet HTTP server connector. Status code 500 returned.");
            response.sendError(500);
        }
    }
}
