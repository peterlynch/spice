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

package org.restlet.test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Service;
import org.restlet.resource.FileRepresentation;

/**
 * Unit test case for the Atom extension.
 * 
 * @author Jerome Louvel
 */
public class AtomTestCase extends TestCase {

    /**
     * Recursively delete a directory.
     * 
     * @param dir
     *            The directory to delete.
     */
    private void deleteDir(File dir) {
        if (dir.exists()) {
            final File[] entries = dir.listFiles();

            for (final File entrie : entries) {
                if (entrie.isDirectory()) {
                    deleteDir(entrie);
                }

                entrie.delete();
            }
        }

        dir.delete();
    }

    public void testAtom() {
        // Create a temporary directory for the tests
        final File testDir = new File(System.getProperty("java.io.tmpdir"),
                "AtomTestCase");
        deleteDir(testDir);
        testDir.mkdir();

        try {
            final Service atomService = new Service(
                    "http://bitworking.org/projects/apptestsite/app.cgi/service/;service_document");
            final Feed atomFeed = atomService.getWorkspaces().get(0)
                    .getCollections().get(0).getFeed();

            // Write the feed into a file.
            final File feedFile = new File(testDir, "feed.xml");
            atomFeed.write(new BufferedOutputStream(new FileOutputStream(
                    feedFile)));

            // Get the service from the file
            final FileRepresentation fileRepresentation = new FileRepresentation(
                    feedFile, MediaType.TEXT_XML);
            final Feed atomFeed2 = new Feed(fileRepresentation);

            assertEquals(atomFeed2.getAuthors().get(0).getName(), atomFeed
                    .getAuthors().get(0).getName());
            assertEquals(atomFeed2.getEntries().get(0).getContent()
                    .getInlineContent().getText(), atomFeed2.getEntries()
                    .get(0).getContent().getInlineContent().getText());
            assertEquals(atomFeed2.getEntries().get(0).getTitle().getContent(),
                    atomFeed2.getEntries().get(0).getTitle().getContent());

        } catch (Exception e) {
            e.printStackTrace();
        }

        deleteDir(testDir);
    }

}
