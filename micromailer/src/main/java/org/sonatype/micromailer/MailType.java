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
package org.sonatype.micromailer;

import java.util.Map;

import javax.activation.DataSource;

/**
 * A mail type descriptor, holding all the needed stuff to have the mail assembled.
 * 
 * @author cstamas
 */
public interface MailType
{
    public static final String SUBJECT_KEY = "subject";

    public static final String BODY_KEY = "body";
    
    public String getTypeId();

    public boolean isBodyIsHtml();

    public boolean isStoreable();

    public String getSubjectTemplate();

    public String getBodyTemplate();

    public Map<String, DataSource> getInlineResources();
}
