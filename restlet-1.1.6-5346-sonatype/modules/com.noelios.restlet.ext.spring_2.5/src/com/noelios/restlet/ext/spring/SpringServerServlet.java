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

package com.noelios.restlet.ext.spring;

import java.util.Enumeration;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.noelios.restlet.component.ChildContext;
import com.noelios.restlet.ext.servlet.ServerServlet;
import com.noelios.restlet.ext.servlet.ServletContextAdapter;

/**
 * Spring specific ServerServlet adapter. This class is similar to the
 * ServerServlet, but instead of creating the used Restlet Application and
 * Restlet Component, it lookups them up from the SpringContext which is found
 * in the ServletContext.
 * 
 * If the Application or Component beans can't be found, the default behavior of
 * the parent class is used.
 * 
 * @author Florian Schwarz
 */
public class SpringServerServlet extends ServerServlet {

    /**
     * Name of the Servlet parameter containing a bean-id of the application to
     * use.
     */
    public static final String APPLICATION_BEAN_PARAM_NAME = "org.restlet.application";

    /**
     * Name of the Servlet parameter containing a bean-id of the component to
     * use.
     */
    public static final String Component_BEAN_PARAM_NAME = "org.restlet.component";

    private static final long serialVersionUID = 110030403435929871L;

    /**
     * Lookups the single Restlet Application used by this Servlet from the
     * SpringContext inside the ServletContext. The bean name looked up is
     * {@link #APPLICATION_BEAN_PARAM_NAME}.
     * 
     * @param parentContext
     *            The parent component context.
     * @return The Restlet-Application to use.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Application createApplication(Context parentContext) {
        Application application = null;

        final String applicationBeanName = getInitParameter(
                SpringServerServlet.APPLICATION_BEAN_PARAM_NAME, null);
        application = (Application) getWebApplicationContext().getBean(
                applicationBeanName);

        if (application != null) {
            // Set the context based on the Servlet's context
            application.setContext(new ChildContext(new ServletContextAdapter(
                    this, parentContext)));
            final ChildContext applicationContext = (ChildContext) application
                    .getContext();

            // Copy all the servlet parameters into the context
            String initParam;

            // Copy all the Web component initialization parameters
            final javax.servlet.ServletConfig servletConfig = getServletConfig();
            for (final Enumeration<String> enum1 = servletConfig
                    .getInitParameterNames(); enum1.hasMoreElements();) {
                initParam = enum1.nextElement();
                applicationContext.getParameters().add(initParam,
                        servletConfig.getInitParameter(initParam));
            }

            // Copy all the Web Application initialization parameters
            for (final Enumeration<String> enum1 = getServletContext()
                    .getInitParameterNames(); enum1.hasMoreElements();) {
                initParam = enum1.nextElement();
                applicationContext.getParameters().add(initParam,
                        getServletContext().getInitParameter(initParam));
            }

        } else {
            application = super.createApplication(parentContext);
        }

        return application;
    }

    /**
     * Lookups the single Restlet Component used by this Servlet from Spring's
     * Context available inside the ServletContext. The bean name looked up is
     * {@link #Component_BEAN_PARAM_NAME}.
     * 
     * @return The Restlet-Component to use.
     */
    @Override
    public Component createComponent() {
        Component component = null;
        final String componentBeanName = getInitParameter(
                Component_BEAN_PARAM_NAME, null);

        // Not mentionned in the Spring javadocs, but getBean surely fails if
        // the argument is null.
        if (componentBeanName != null) {
            try {
                component = (Component) getWebApplicationContext().getBean(
                        componentBeanName);
            } catch (BeansException be) {
                // The bean has not been found, let the parent create it.
            }
        }

        if (component == null) {
            component = super.createComponent();
        }

        return component;
    }

    /**
     * Get the Spring WebApplicationContext from the ServletContext. (by hand
     * would be webApplicationContext applicationContext =
     * (WebApplicationContext)
     * getServletContext().getAttribute(WebApplicationContext
     * .ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);)
     * 
     * @return The Spring WebApplicationContext.
     */
    public WebApplicationContext getWebApplicationContext() {
        return WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
    }

}
