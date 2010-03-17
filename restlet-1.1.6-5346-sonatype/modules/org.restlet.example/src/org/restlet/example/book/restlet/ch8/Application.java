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

package org.restlet.example.book.restlet.ch8;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch8.data.db4o.Db4oFacade;
import org.restlet.example.book.restlet.ch8.objects.ObjectsFacade;
import org.restlet.example.book.restlet.ch8.resources.ContactResource;
import org.restlet.example.book.restlet.ch8.resources.ContactsResource;
import org.restlet.example.book.restlet.ch8.resources.FeedResource;
import org.restlet.example.book.restlet.ch8.resources.FeedsResource;
import org.restlet.example.book.restlet.ch8.resources.MailResource;
import org.restlet.example.book.restlet.ch8.resources.MailRootResource;
import org.restlet.example.book.restlet.ch8.resources.MailboxResource;
import org.restlet.example.book.restlet.ch8.resources.MailboxesResource;
import org.restlet.example.book.restlet.ch8.resources.MailsResource;
import org.restlet.example.book.restlet.ch8.resources.UserResource;
import org.restlet.example.book.restlet.ch8.resources.UsersResource;

/**
 * The main Web application.
 */
public class Application extends org.restlet.Application {

    /**
     * Returns a Properties instance loaded from the given URI.
     * 
     * @param propertiesUri
     *            The URI of the properties file.
     * @return A Properties instance loaded from the given URI.
     * @throws IOException
     */
    public static Properties getProperties(String propertiesUri)
            throws IOException {
        Reference reference = new Reference(propertiesUri);
        Response response = new Client(reference.getSchemeProtocol())
                .get(reference);
        if (!(response.getStatus().isSuccess() && response.isEntityAvailable())) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot access to the configuration file: \"");
            stringBuilder.append(propertiesUri);
            stringBuilder.append("\"");
            throw new IllegalArgumentException(stringBuilder.toString());
        }

        Properties properties = new Properties();
        properties.load(response.getEntity().getStream());
        return properties;
    }

    public static void main(String... args) throws Exception {
        // Create a component with an HTTP server connector
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8585);
        component.getClients().add(Protocol.FILE);
        component.getClients().add(Protocol.CLAP);
        component.getClients().add(Protocol.HTTP);
        // Attach the application to the default host and start it
        component.getDefaultHost().attach("/rmep", new Application());
        component.start();
    }

    /** Facade object for all access to data. */
    private final ObjectsFacade dataFacade;

    /** Freemarker configuration object. */
    private freemarker.template.Configuration fmc;

    /** File path to the Db4o database. */
    private final String dbFilePath;

    /**
     * File path of the root directory of the web files (images, templates,
     * etc).
     */
    private final String webRootPath;

    /**
     * Constructor.
     * 
     * @throws IOException
     */
    public Application() throws IOException {
        // List of protocols required by the application.
        getConnectorService().getClientProtocols().add(Protocol.FILE);
        getConnectorService().getClientProtocols().add(Protocol.CLAP);
        getConnectorService().getClientProtocols().add(Protocol.HTTP);

        // Look for the configuration file in the classpath
        Properties properties = getProperties("clap://class/config/mailApplication.properties");

        this.dbFilePath = properties.getProperty("db4o.file.path");
        this.webRootPath = properties.getProperty("web.root.path");

        /** Create and chain the Objects and Data facades. */
        this.dataFacade = new ObjectsFacade(new Db4oFacade(dbFilePath));
        // Check that at least one administrator exists in the database.
        this.dataFacade.initAdmin();

        try {
            final File templateDir = new File(webRootPath + "/tmpl");
            this.fmc = new freemarker.template.Configuration();
            this.fmc.setDirectoryForTemplateLoading(templateDir);
        } catch (Exception e) {
            getLogger().severe("Unable to configure FreeMarker.");
            e.printStackTrace();
        }

        getMetadataService().addExtension("xml",
                MediaType.APPLICATION_ATOM_XML, true);
    }

    @Override
    public Restlet createRoot() {
        final Router router = new Router(getContext());

        final RmepGuard guard = new RmepGuard(getContext(),
                ChallengeScheme.HTTP_BASIC, "rmep", this.dataFacade);

        // Secure the root of the application and only this resource. It allows
        // anonymous access to all other resources and authentication at the top
        // of our application's hierarchy of URIs. It also makes the assumption
        // that common Internet browsers preemptively authenticate future
        // requests made to "sub" URIs.
        guard.setNext(MailRootResource.class);

        // Add a route for the MailRoot resource
        router.attachDefault(guard);

        final Directory imgDirectory = new Directory(getContext(),
                LocalReference.createFileReference(webRootPath + "/images"));
        // Add a route for the image resources
        router.attach("/images", imgDirectory);

        final Directory cssDirectory = new Directory(getContext(),
                LocalReference
                        .createFileReference(webRootPath + "/stylesheets"));
        // Add a route for the CSS resources
        router.attach("/stylesheets", cssDirectory);

        // Add a route for a Users resource
        router.attach("/users", UsersResource.class);

        // Add a route for a User resource
        router.attach("/users/{userId}", UserResource.class);

        // Add a route for a Mailboxes resource
        router.attach("/mailboxes", MailboxesResource.class);

        // Add a router for access to mailbox
        final Router mailboxRouter = new Router(getContext());

        // Add a route for a Mailbox resource
        mailboxRouter.attachDefault(MailboxResource.class);

        // Add a route for a Contacts resource
        mailboxRouter.attach("/contacts", ContactsResource.class);

        // Add a route for a Contact resource
        mailboxRouter.attach("/contacts/{contactId}", ContactResource.class);

        // Add a route for a Mails resource
        mailboxRouter.attach("/mails", MailsResource.class);

        // Add a route for a Mail resource
        mailboxRouter.attach("/mails/{mailId}", MailResource.class);

        // Add a route for a Feeds resource
        mailboxRouter.attach("/feeds", FeedsResource.class);

        // Add a route for a Feed resource
        mailboxRouter.attach("/feeds/{feedId}", FeedResource.class);

        // Add a route for a Mailbox resource
        router.attach("/mailboxes/{mailboxId}", mailboxRouter);

        return router;
    }

    /**
     * Returns the freemarker configuration object.
     * 
     * @return the freemarker configuration object.
     */
    public freemarker.template.Configuration getFmc() {
        return this.fmc;
    }

    /**
     * Returns the data facade.
     * 
     * @return the data facade.
     */
    public ObjectsFacade getObjectsFacade() {
        return this.dataFacade;
    }
}
