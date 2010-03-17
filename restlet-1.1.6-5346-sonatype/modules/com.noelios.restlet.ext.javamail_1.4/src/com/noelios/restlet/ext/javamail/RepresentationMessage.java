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

package com.noelios.restlet.ext.javamail;

import java.io.IOException;
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * JavaMail MIME message based on an XML representation. For the XML format, see
 * the {@link JavaMailClientHelper} class.
 * 
 * @author Jerome Louvel
 */
public class RepresentationMessage extends MimeMessage {

    /**
     * Creates a JavaMail message by parsing an XML representation.
     * 
     * @param xmlMessage
     *            The XML message to parse.
     * @param session
     *            The current JavaMail session.
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     */
    public RepresentationMessage(Representation xmlMessage, Session session)
            throws IOException, AddressException, MessagingException {
        super(session);
        final DomRepresentation dom = new DomRepresentation(xmlMessage);
        final Document email = dom.getDocument();
        final Element root = (Element) email.getElementsByTagName("email")
                .item(0);
        final Element header = (Element) root.getElementsByTagName("head")
                .item(0);
        final String subject = header.getElementsByTagName("subject").item(0)
                .getTextContent();
        final String from = header.getElementsByTagName("from").item(0)
                .getTextContent();

        final NodeList toList = header.getElementsByTagName("to");
        final String[] to = new String[toList.getLength()];
        for (int i = 0; i < toList.getLength(); i++) {
            to[i] = toList.item(i).getTextContent();
        }

        final NodeList ccList = header.getElementsByTagName("cc");
        final String[] cc = new String[ccList.getLength()];
        for (int i = 0; i < ccList.getLength(); i++) {
            cc[i] = ccList.item(i).getTextContent();
        }

        final NodeList bccList = header.getElementsByTagName("bcc");
        final String[] bcc = new String[bccList.getLength()];
        for (int i = 0; i < bccList.getLength(); i++) {
            bcc[i] = bccList.item(i).getTextContent();
        }

        final String text = root.getElementsByTagName("body").item(0)
                .getTextContent();

        // Set the FROM and TO fields
        setFrom(new InternetAddress(from));

        for (final String element : to) {
            addRecipient(Message.RecipientType.TO, new InternetAddress(element));
        }

        for (final String element : cc) {
            addRecipient(Message.RecipientType.CC, new InternetAddress(element));
        }

        for (final String element : bcc) {
            addRecipient(Message.RecipientType.BCC,
                    new InternetAddress(element));
        }

        // Set the subject and content text
        setSubject(subject);
        setText(text);
        setSentDate(new Date());
        saveChanges();
    }

    /**
     * Constructor.
     * 
     * @param session
     *            The current JavaMail session.
     */
    public RepresentationMessage(Session session) {
        super(session);
    }

}
