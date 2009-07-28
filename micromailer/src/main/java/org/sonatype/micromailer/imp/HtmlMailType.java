/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.micromailer.imp;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.micromailer.MailType;

/**
 * The html mail type.
 */
@Component( role = MailType.class, hint = HtmlMailType.HTML_TYPE_ID )
public class HtmlMailType
    extends AbstractMailType
{
    public static final String HTML_TYPE_ID = "html";

    public HtmlMailType()
    {
        setTypeId( HTML_TYPE_ID );

        setBodyIsHtml( true );

        setSubjectTemplate( "$" + SUBJECT_KEY );

        setBodyTemplate( "$" + BODY_KEY );
    }
}
