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

package org.restlet.gwt.engine.http;

import java.io.IOException;
import java.util.Collection;

import org.restlet.gwt.data.CharacterSet;
import org.restlet.gwt.data.Dimension;
import org.restlet.gwt.data.Parameter;
import org.restlet.gwt.data.Reference;

/**
 * HTTP-style header manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public class HttpUtils {
    /**
     * Appends a source string as an HTTP comment.
     * 
     * @param source
     *            The source string to format.
     * @param destination
     *            The appendable destination.
     * @throws IOException
     */
    public static StringBuilder appendComment(CharSequence source,
            StringBuilder destination) throws Exception {
        destination.append('(');

        char c;
        for (int i = 0; i < source.length(); i++) {
            c = source.charAt(i);

            if (c == '(') {
                destination.append("\\(");
            } else if (c == ')') {
                destination.append("\\)");
            } else if (c == '\\') {
                destination.append("\\\\");
            } else {
                destination.append(c);
            }
        }

        destination.append(')');
        return destination;
    }

    /**
     * Appends a source string as an HTTP quoted string.
     * 
     * @param source
     *            The unquoted source string.
     * @param destination
     *            The destination to append to.
     * @throws IOException
     */
    public static StringBuilder appendQuote(CharSequence source,
            StringBuilder destination) throws Exception {
        destination.append('"');

        char c;
        for (int i = 0; i < source.length(); i++) {
            c = source.charAt(i);

            if (c == '"') {
                destination.append("\\\"");
            } else if (c == '\\') {
                destination.append("\\\\");
            } else {
                destination.append(c);
            }
        }

        destination.append('"');
        return destination;
    }

    /**
     * Appends a source string as an URI encoded string.
     * 
     * @param source
     *            The source string to format.
     * @param destination
     *            The appendable destination.
     * @param characterSet
     *            The supported character encoding.
     * @throws IOException
     */
    public static StringBuilder appendUriEncoded(CharSequence source,
            StringBuilder destination, CharacterSet characterSet)
            throws Exception {
        destination.append(Reference.encode(source.toString(), characterSet));
        return destination;
    }

    /**
     * Creates a parameter.
     * 
     * @param name
     *            The parameter name buffer.
     * @param value
     *            The parameter value buffer (can be null).
     * @return The created parameter.
     * @throws IOException
     */
    public static Parameter createParameter(CharSequence name,
            CharSequence value) {
        if (value != null) {
            return new Parameter(name.toString(), value.toString());
        } else {
            return new Parameter(name.toString(), null);
        }
    }

    /**
     * Creates a vary header from the given dimensions.
     * 
     * @param dimensions
     *            The dimensions to copy to the response.
     * @return Returns the Vary header or null, if dimensions is null or empty.
     */
    public static String createVaryHeader(Collection<Dimension> dimensions) {
        String vary = null;
        if ((dimensions != null) && !dimensions.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            boolean first = true;

            if (dimensions.contains(Dimension.CLIENT_ADDRESS)
                    || dimensions.contains(Dimension.TIME)
                    || dimensions.contains(Dimension.UNSPECIFIED)) {
                // From an HTTP point of view the representations can
                // vary in unspecified ways
                vary = "*";
            } else {
                for (final Dimension dim : dimensions) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }

                    if (dim == Dimension.CHARACTER_SET) {
                        sb.append(HttpConstants.HEADER_ACCEPT_CHARSET);
                    } else if (dim == Dimension.CLIENT_AGENT) {
                        sb.append(HttpConstants.HEADER_USER_AGENT);
                    } else if (dim == Dimension.ENCODING) {
                        sb.append(HttpConstants.HEADER_ACCEPT_ENCODING);
                    } else if (dim == Dimension.LANGUAGE) {
                        sb.append(HttpConstants.HEADER_ACCEPT_LANGUAGE);
                    } else if (dim == Dimension.MEDIA_TYPE) {
                        sb.append(HttpConstants.HEADER_ACCEPT);
                    } else if (dim == Dimension.AUTHORIZATION) {
                        sb.append(HttpConstants.HEADER_AUTHORIZATION);
                    }
                }
                vary = sb.toString();
            }
        }
        return vary;
    }

    /**
     * Formats a product description.
     * 
     * @param nameToken
     *            The product name token.
     * @param versionToken
     *            The product version token.
     * @param destination
     *            The appendable destination;
     * @throws IOException
     */
    public static void formatProduct(CharSequence nameToken,
            CharSequence versionToken, StringBuilder destination)
            throws Exception {
        if (!isToken(nameToken)) {
            throw new IllegalArgumentException(
                    "Invalid product name detected. Only token characters are allowed.");
        } else {
            destination.append(nameToken);

            if (versionToken != null) {
                if (!isToken(versionToken)) {
                    throw new IllegalArgumentException(
                            "Invalid product version detected. Only token characters are allowed.");
                } else {
                    destination.append('/').append(versionToken);
                }
            }
        }
    }

    /**
     * Indicates if the given character is alphabetical (a-z or A-Z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is alphabetical (a-z or A-Z).
     */
    public static boolean isAlpha(int character) {
        return isUpperCase(character) || isLowerCase(character);
    }

    /**
     * Indicates if the given character is in ASCII range.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is in ASCII range.
     */
    public static boolean isAsciiChar(int character) {
        return (character >= 0) && (character <= 127);
    }

    /**
     * Indicates if the given character is a carriage return.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a carriage return.
     */
    public static boolean isCarriageReturn(int character) {
        return (character == 13);
    }

    /**
     * Indicates if the given character is a control character.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a control character.
     */
    public static boolean isControlChar(int character) {
        return ((character >= 0) && (character <= 31)) || (character == 127);
    }

    /**
     * Indicates if the given character is a digit (0-9).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a digit (0-9).
     */
    public static boolean isDigit(int character) {
        return (character >= '0') && (character <= '9');
    }

    /**
     * Indicates if the given character is a double quote.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a double quote.
     */
    public static boolean isDoubleQuote(int character) {
        return (character == 34);
    }

    /**
     * Indicates if the given character is an horizontal tab.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is an horizontal tab.
     */
    public static boolean isHorizontalTab(int character) {
        return (character == 9);
    }

    /**
     * Indicates if the given character is a line feed.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a line feed.
     */
    public static boolean isLineFeed(int character) {
        return (character == 10);
    }

    /**
     * Indicates if the given character is lower case (a-z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is lower case (a-z).
     */
    public static boolean isLowerCase(int character) {
        return (character >= 'a') && (character <= 'z');
    }

    /**
     * Indicates if the given character is a separator.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a separator.
     */
    public static boolean isSeparator(int character) {
        switch (character) {
        case '(':
        case ')':
        case '<':
        case '>':
        case '@':
        case ',':
        case ';':
        case ':':
        case '\\':
        case '"':
        case '/':
        case '[':
        case ']':
        case '?':
        case '=':
        case '{':
        case '}':
        case ' ':
        case '\t':
            return true;

        default:
            return false;
        }
    }

    /**
     * Indicates if the given character is a space.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a space.
     */
    public static boolean isSpace(int character) {
        return (character == 32);
    }

    /**
     * Indicates if the given character is textual (ASCII and not a control
     * character).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is textual (ASCII and not a control
     *         character).
     */
    public static boolean isText(int character) {
        return isAsciiChar(character) && !isControlChar(character);
    }

    /**
     * Indicates if the token is valid.<br>
     * Only contains valid token characters.
     * 
     * @param token
     *            The token to check
     * @return True if the token is valid.
     */
    public static boolean isToken(CharSequence token) {
        for (int i = 0; i < token.length(); i++) {
            if (!isTokenChar(token.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Indicates if the given character is a token character (text and not a
     * separator).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a token character (text and not a
     *         separator).
     */
    public static boolean isTokenChar(int character) {
        return isText(character) && !isSeparator(character);
    }

    /**
     * Indicates if the given character is upper case (A-Z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is upper case (A-Z).
     */
    public static boolean isUpperCase(int character) {
        return (character >= 'A') && (character <= 'Z');
    }

}
