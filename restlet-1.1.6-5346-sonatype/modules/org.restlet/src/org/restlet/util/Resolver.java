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

package org.restlet.util;

import java.util.Date;
import java.util.Map;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Resolves a name into a value.
 * 
 * @author Jerome Louvel
 */
public abstract class Resolver<T> {

    /**
     * Resolves variable values based on a request and a response.
     * 
     * @author Jerome Louvel
     */
    private static class CallResolver extends Resolver<String> {
        /** The request to use as a model. */
        private final Request request;

        /** The response to use as a model. */
        private final Response response;

        /**
         * Constructor.
         * 
         * @param request
         *            The request to use as a model.
         * @param response
         *            The response to use as a model.
         */
        public CallResolver(Request request, Response response) {
            this.request = request;
            this.response = response;
        }

        /**
         * Returns the content corresponding to a reference property.
         * 
         * @param partName
         *            The variable sub-part name.
         * @param reference
         *            The reference to use as a model.
         * @return The content corresponding to a reference property.
         */
        private String getReferenceContent(String partName, Reference reference) {
            String result = null;

            if (reference != null) {
                if (partName.equals("a")) {
                    result = reference.getAuthority();
                } else if (partName.startsWith("b")) {
                    result = getReferenceContent(partName.substring(1),
                            reference.getBaseRef());
                } else if (partName.equals("e")) {
                    result = reference.getRelativePart();
                } else if (partName.equals("f")) {
                    result = reference.getFragment();
                } else if (partName.equals("h")) {
                    result = reference.getHostIdentifier();
                } else if (partName.equals("i")) {
                    result = reference.getIdentifier();
                } else if (partName.equals("p")) {
                    result = reference.getPath();
                } else if (partName.equals("q")) {
                    result = reference.getQuery();
                } else if (partName.equals("r")) {
                    result = reference.getRemainingPart();
                }
            }

            return result;
        }

        @Override
        public String resolve(String variableName) {
            String result = null;

            // Check for a matching request attribute
            if (this.request != null) {
                final Object variable = this.request.getAttributes().get(
                        variableName);
                if (variable != null) {
                    result = variable.toString();
                }
            }

            // Check for a matching response attribute
            if ((result == null) && (this.response != null)
                    && this.response.getAttributes().containsKey(variableName)) {
                result = this.response.getAttributes().get(variableName)
                        .toString();
            }

            // Check for a matching request or response property
            if (result == null) {
                if (this.request != null) {
                    if (variableName.equals("c")) {
                        result = Boolean
                                .toString(this.request.isConfidential());
                    } else if (variableName.equals("cia")) {
                        result = this.request.getClientInfo().getAddress();
                    } else if (variableName.equals("cig")) {
                        result = this.request.getClientInfo().getAgent();
                    } else if (variableName.equals("cri")) {
                        ChallengeResponse cr = this.request
                                .getChallengeResponse();
                        if (cr != null) {
                            result = cr.getIdentifier();
                        }
                    } else if (variableName.equals("crs")) {
                        ChallengeResponse cr = this.request
                                .getChallengeResponse();
                        if (cr != null && cr.getScheme() != null) {
                            result = cr.getScheme().getTechnicalName();
                        }
                    } else if (variableName.equals("d")) {
                        result = DateUtils.format(new Date(),
                                DateUtils.FORMAT_RFC_1123.get(0));
                    } else if (variableName.equals("ecs")) {
                        if ((this.request.getEntity() != null)
                                && (this.request.getEntity().getCharacterSet() != null)) {
                            result = this.request.getEntity().getCharacterSet()
                                    .getName();
                        }
                    } else if (variableName.equals("ee")) {
                        if ((this.request.getEntity() != null)
                                && (!this.request.getEntity().getEncodings()
                                        .isEmpty())) {
                            final StringBuilder value = new StringBuilder();
                            for (int i = 0; i < this.request.getEntity()
                                    .getEncodings().size(); i++) {
                                if (i > 0) {
                                    value.append(", ");
                                }
                                value.append(this.request.getEntity()
                                        .getEncodings().get(i).getName());
                            }
                            result = value.toString();
                        }
                    } else if (variableName.equals("eed")) {
                        if ((this.request.getEntity() != null)
                                && (this.request.getEntity()
                                        .getExpirationDate() != null)) {
                            result = DateUtils.format(this.request.getEntity()
                                    .getExpirationDate(),
                                    DateUtils.FORMAT_RFC_1123.get(0));
                        }
                    } else if (variableName.equals("el")) {
                        if ((this.request.getEntity() != null)
                                && (!this.request.getEntity().getLanguages()
                                        .isEmpty())) {
                            final StringBuilder value = new StringBuilder();
                            for (int i = 0; i < this.request.getEntity()
                                    .getLanguages().size(); i++) {
                                if (i > 0) {
                                    value.append(", ");
                                }
                                value.append(this.request.getEntity()
                                        .getLanguages().get(i).getName());
                            }
                            result = value.toString();
                        }
                    } else if (variableName.equals("emd")) {
                        if ((this.request.getEntity() != null)
                                && (this.request.getEntity()
                                        .getModificationDate() != null)) {
                            result = DateUtils.format(this.request.getEntity()
                                    .getModificationDate(),
                                    DateUtils.FORMAT_RFC_1123.get(0));
                        }
                    } else if (variableName.equals("emt")) {
                        if ((this.request.getEntity() != null)
                                && (this.request.getEntity().getMediaType() != null)) {
                            result = this.request.getEntity().getMediaType()
                                    .getName();
                        }
                    } else if (variableName.equals("es")) {
                        if ((this.request.getEntity() != null)
                                && (this.request.getEntity().getSize() != -1)) {
                            result = Long.toString(this.request.getEntity()
                                    .getSize());
                        }
                    } else if (variableName.equals("et")) {
                        if ((this.request.getEntity() != null)
                                && (this.request.getEntity().getTag() != null)) {
                            result = this.request.getEntity().getTag()
                                    .getName();
                        }
                    } else if (variableName.startsWith("f")) {
                        result = getReferenceContent(variableName.substring(1),
                                this.request.getReferrerRef());
                    } else if (variableName.startsWith("h")) {
                        result = getReferenceContent(variableName.substring(1),
                                this.request.getHostRef());
                    } else if (variableName.equals("m")) {
                        if (this.request.getMethod() != null) {
                            result = this.request.getMethod().getName();
                        }
                    } else if (variableName.startsWith("o")) {
                        result = getReferenceContent(variableName.substring(1),
                                this.request.getRootRef());
                    } else if (variableName.equals("p")) {
                        if (this.request.getProtocol() != null) {
                            result = this.request.getProtocol().getName();
                        }
                    } else if (variableName.startsWith("r")) {
                        result = getReferenceContent(variableName.substring(1),
                                this.request.getResourceRef());
                    }
                }

                if ((result == null) && (this.response != null)) {
                    if (variableName.equals("ECS")) {
                        if ((this.response.getEntity() != null)
                                && (this.response.getEntity().getCharacterSet() != null)) {
                            result = this.response.getEntity()
                                    .getCharacterSet().getName();
                        }
                    } else if (variableName.equals("EE")) {
                        if ((this.response.getEntity() != null)
                                && (!this.response.getEntity().getEncodings()
                                        .isEmpty())) {
                            final StringBuilder value = new StringBuilder();
                            for (int i = 0; i < this.response.getEntity()
                                    .getEncodings().size(); i++) {
                                if (i > 0) {
                                    value.append(", ");
                                }
                                value.append(this.response.getEntity()
                                        .getEncodings().get(i).getName());
                            }
                            result = value.toString();
                        }
                    } else if (variableName.equals("EED")) {
                        if ((this.response.getEntity() != null)
                                && (this.response.getEntity()
                                        .getExpirationDate() != null)) {
                            result = DateUtils.format(this.response.getEntity()
                                    .getExpirationDate(),
                                    DateUtils.FORMAT_RFC_1123.get(0));
                        }
                    } else if (variableName.equals("EL")) {
                        if ((this.response.getEntity() != null)
                                && (!this.response.getEntity().getLanguages()
                                        .isEmpty())) {
                            final StringBuilder value = new StringBuilder();
                            for (int i = 0; i < this.response.getEntity()
                                    .getLanguages().size(); i++) {
                                if (i > 0) {
                                    value.append(", ");
                                }
                                value.append(this.response.getEntity()
                                        .getLanguages().get(i).getName());
                            }
                            result = value.toString();
                        }
                    } else if (variableName.equals("EMD")) {
                        if ((this.response.getEntity() != null)
                                && (this.response.getEntity()
                                        .getModificationDate() != null)) {
                            result = DateUtils.format(this.response.getEntity()
                                    .getModificationDate(),
                                    DateUtils.FORMAT_RFC_1123.get(0));
                        }
                    } else if (variableName.equals("EMT")) {
                        if ((this.response.getEntity() != null)
                                && (this.response.getEntity().getMediaType() != null)) {
                            result = this.response.getEntity().getMediaType()
                                    .getName();
                        }
                    } else if (variableName.equals("ES")) {
                        if ((this.response.getEntity() != null)
                                && (this.response.getEntity().getSize() != -1)) {
                            result = Long.toString(this.response.getEntity()
                                    .getSize());
                        }
                    } else if (variableName.equals("ET")) {
                        if ((this.response.getEntity() != null)
                                && (this.response.getEntity().getTag() != null)) {
                            result = this.response.getEntity().getTag()
                                    .getName();
                        }
                    } else if (variableName.startsWith("R")) {
                        result = getReferenceContent(variableName.substring(1),
                                this.response.getLocationRef());
                    } else if (variableName.equals("S")) {
                        if (this.response.getStatus() != null) {
                            result = Integer.toString(this.response.getStatus()
                                    .getCode());
                        }
                    } else if (variableName.equals("SIA")) {
                        result = this.response.getServerInfo().getAddress();
                    } else if (variableName.equals("SIG")) {
                        result = this.response.getServerInfo().getAgent();
                    } else if (variableName.equals("SIP")) {
                        if (this.response.getServerInfo().getPort() != -1) {
                            result = Integer.toString(this.response
                                    .getServerInfo().getPort());
                        }
                    }
                }
            }

            return result;
        }
    }

    /**
     * Resolves variable values based on a map.
     * 
     * @author Jerome Louvel
     */
    private static class MapResolver extends Resolver<String> {
        /** The variables to use when formatting. */
        private final Map<String, ?> map;

        /**
         * Constructor.
         * 
         * @param map
         *            The variables to use when formatting.
         */
        public MapResolver(Map<String, ?> map) {
            this.map = map;
        }

        @Override
        public String resolve(String variableName) {
            final Object value = this.map.get(variableName);
            return (value == null) ? null : value.toString();
        }
    }

    /**
     * Creates a resolver that is based on a given map.
     * 
     * @param map
     *            Map between names and values.
     * @return The map resolver.
     */
    public static Resolver<String> createResolver(Map<String, ?> map) {
        return new MapResolver(map);
    }

/**
     * Creates a resolver that is based on a call (request, response couple).
     * 
     * <table>
     * <tr>
     * <th>Model property</th>
     * <th>Variable name</th>
     * <th>Content type</th>
     * </tr>
     * <tr>
     * <td>request.confidential</td>
     * <td>c</td>
     * <td>boolean (true|false)</td>
     * </tr>
     * <tr>
     * <td>request.clientInfo.address</td>
     * <td>cia</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.clientInfo.agent</td>
     * <td>cig</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.challengeResponse.identifier</td>
     * <td>cri</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.challengeResponse.scheme</td>
     * <td>crs</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.date</td>
     * <td>d</td>
     * <td>Date (HTTP format)</td>
     * </tr>
     * <tr>
     * <td>request.entity.characterSet</td>
     * <td>ecs</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.entity.characterSet</td>
     * <td>ECS</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.entity.encoding</td>
     * <td>ee</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.entity.encoding</td>
     * <td>EE</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.entity.expirationDate</td>
     * <td>eed</td>
     * <td>Date (HTTP format)</td>
     * </tr>
     * <tr>
     * <td>response.entity.expirationDate</td>
     * <td>EED</td>
     * <td>Date (HTTP format)</td>
     * </tr>
     * <tr>
     * <td>request.entity.language</td>
     * <td>el</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.entity.language</td>
     * <td>EL</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.entity.modificationDate</td>
     * <td>emd</td>
     * <td>Date (HTTP format)</td>
     * </tr>
     * <tr>
     * <td>response.entity.modificationDate</td>
     * <td>EMD</td>
     * <td>Date (HTTP format)</td>
     * </tr>
     * <tr>
     * <td>request.entity.mediaType</td>
     * <td>emt</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.entity.mediaType</td>
     * <td>EMT</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.entity.size</td>
     * <td>es</td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td>response.entity.size</td>
     * <td>ES</td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td>request.entity.tag</td>
     * <td>et</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.entity.tag</td>
     * <td>ET</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.referrerRef</td>
     * <td>f*</td>
     * <td>Reference (see table below variable name sub-parts)</td>
     * </tr>
     * <tr>
     * <td>request.hostRef</td>
     * <td>h*</td>
     * <td>Reference (see table below variable name sub-parts)</td>
     * </tr>
     * <tr>
     * <td>request.method</td>
     * <td>m</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.rootRef</td>
     * <td>o*</td>
     * <td>Reference (see table below variable name sub-parts)</td>
     * </tr>
     * <tr>
     * <td>request.protocol</td>
     * <td>p</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>request.resourceRef</td>
     * <td>r*</td>
     * <td>Reference (see table below variable name sub-parts)</td>
     * </tr>
     * <tr>
     * <td>response.redirectRef</td>
     * <td>R*</td>
     * <td>Reference (see table below variable name sub-parts)</td>
     * </tr>
     * <tr>
     * <td>response.status</td>
     * <td>S</td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td>response.serverInfo.address</td>
     * <td>SIA</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.serverInfo.agent</td>
     * <td>SIG</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>response.serverInfo.port</td>
     * <td>SIP</td>
     * <td>Integer</td>
     * </tr>
     * </table> <br>
     * 
     * Below is the list of name sub-parts, for Reference variables, that can
     * replace the asterix in the variable names above:<br>
     * <br>
     * 
     * <table>
     * <tr>
     * <th>Reference property</th>
     * <th>Sub-part name</th>
     * <th>Content type</th>
     * </tr>
     * <tr>
     * <td>authority</td>
     * <td>a</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>baseRef</td>
     * <td>b*</td>
     * <td>Reference</td>
     * </tr>
     * <tr>
     * <td>relativePart</td>
     * <td>e</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>fragment</td>
     * <td>f</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>hostIdentifier</td>
     * <td>h</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>identifier</td>
     * <td>i</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>path</td>
     * <td>p</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>query</td>
     * <td>q</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>remainingPart</td>
     * <td>r</td>
     * <td>String</td>
     * </tr>
     * </table>
     * 
     * @param request
     *                The request.
     * @param response
     *                The response.
     * @return The call resolver.
     */
    public static Resolver<String> createResolver(Request request,
            Response response) {
        return new CallResolver(request, response);
    }

    /**
     * Resolves a name into a value.
     * 
     * @param name
     *            The name to resolve.
     * @return The resolved value.
     */
    public abstract T resolve(String name);

}
