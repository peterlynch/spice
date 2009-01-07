/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import javax.activation.DataSource;

import org.sonatype.micromailer.MailType;

/**
 * The simple default mail type.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultMailType
    implements MailType
{
    public static final String DEFAULT_TYPE_ID = "default";

    public static final String SUBJECT_KEY = "subject";

    public static final String BODY_KEY = "body";

    private String typeId;

    private boolean bodyIsHtml;

    private boolean storeable;

    private String subjectTemplate;

    private String bodyTemplate;

    private Map<String, DataSource> inlineResources;

    public DefaultMailType()
    {
        super();

        this.typeId = "default";

        this.bodyIsHtml = false;

        this.subjectTemplate = "$" + SUBJECT_KEY;

        this.bodyTemplate = "$" + BODY_KEY;

        this.inlineResources = new HashMap<String, DataSource>();
    }

    public String getTypeId()
    {
        return typeId;
    }

    public void setTypeId( String typeId )
    {
        this.typeId = typeId;
    }

    public boolean isBodyIsHtml()
    {
        return bodyIsHtml;
    }

    public boolean isStoreable()
    {
        return storeable;
    }

    public void setStoreable( boolean storeable )
    {
        this.storeable = storeable;
    }

    public void setBodyIsHtml( boolean bodyIsHtml )
    {
        this.bodyIsHtml = bodyIsHtml;
    }

    public String getSubjectTemplate()
    {
        return subjectTemplate;
    }

    public void setSubjectTemplate( String subjectTemplate )
    {
        this.subjectTemplate = subjectTemplate;
    }

    public String getBodyTemplate()
    {
        return bodyTemplate;
    }

    public void setBodyTemplate( String bodyTemplate )
    {
        this.bodyTemplate = bodyTemplate;
    }

    public Map<String, DataSource> getInlineResources()
    {
        return inlineResources;
    }

    public void setInlineResources( Map<String, DataSource> inlineResources )
    {
        this.inlineResources = inlineResources;
    }

}
