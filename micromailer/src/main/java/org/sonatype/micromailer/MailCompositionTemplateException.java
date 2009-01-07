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
package org.sonatype.micromailer;

/**
 * Thrown in case templating is unsuccesful during mail composition.
 * 
 * @author cstamas
 */
public class MailCompositionTemplateException
    extends MailCompositionException
{
    private static final long serialVersionUID = -6035479489862032914L;

    public MailCompositionTemplateException( String msg )
    {
        super( msg );
    }

    public MailCompositionTemplateException( String msg, Throwable ex )
    {
        super( msg, ex );
    }
}
