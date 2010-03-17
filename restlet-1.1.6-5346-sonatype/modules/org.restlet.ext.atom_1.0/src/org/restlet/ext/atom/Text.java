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

package org.restlet.ext.atom;

import static org.restlet.ext.atom.Feed.ATOM_NAMESPACE;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.restlet.data.MediaType;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A Text construct contains human-readable text, usually in small quantities.
 * The content of Text constructs is Language-Sensitive.
 * 
 * @author Jerome Louvel
 */
public class Text {

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @param namespace
     *            The element namespace URI.
     * @param localName
     *            The local name of the element.
     * @throws SAXException
     */
    public static void writeElement(XmlWriter writer, Date date,
            String namespace, String localName) throws SAXException {
        writer.startElement(namespace, localName);

        if (date != null) {
            final SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'hh:mm:ssZ");
            writer.characters(dateFormat.format(date));
        }

        writer.endElement(namespace, localName);
    }

    /**
     * The content.
     */
    private volatile String content;

    /**
     * The content type.
     */
    private volatile MediaType type;

    /**
     * Constructor.
     * 
     * @param type
     *            The content type.
     */
    public Text(MediaType type) {
        this(type, null);
    }

    /**
     * Constructor.
     * 
     * @param type
     *            The content type.
     * @param content
     *            The content.
     */
    public Text(MediaType type, String content) {
        this.type = type;
        this.content = content;
    }

    /**
     * Returns the content.
     * 
     * @return The content.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Returns the content type.
     * 
     * @return The content type.
     */
    public MediaType getType() {
        return this.type;
    }

    /**
     * Sets the content.
     * 
     * @param content
     *            The content.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets the content type.
     * 
     * @param type
     *            The content type.
     */
    public void setType(MediaType type) {
        this.type = type;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @param localName
     *            The local name of the element.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer, String localName)
            throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();
        String type = null;

        if ((getType() != null) && (getType().getSubType() != null)) {
            if (getType().getSubType().contains("xhtml")) {
                type = "xhtml";
            } else if (getType().getSubType().contains("html")) {
                type = "html";
            }
        }

        if (type == null) {
            type = "text";
        }

        attributes.addAttribute("", "type", null, "text", type);

        if (getContent() != null) {
            writer.dataElement(ATOM_NAMESPACE, localName, null, attributes,
                    getContent());
        } else {
            writer.emptyElement(ATOM_NAMESPACE, localName, null, attributes);
        }
    }

}
