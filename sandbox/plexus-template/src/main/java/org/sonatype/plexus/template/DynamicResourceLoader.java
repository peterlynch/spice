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
package org.sonatype.plexus.template;

import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

public class DynamicResourceLoader
    extends ResourceLoader
{
    private static TemplateLoader loader;       

    public static void setTemplateLoader( TemplateLoader staticLoader )
    {
        loader = staticLoader;
    }

    public void init( ExtendedProperties properties )
    {
    }
    
    public long getLastModified( Resource resource )
    {
        return 0;
    }

    public InputStream getResourceStream( String resource )
        throws ResourceNotFoundException
    {
        return loader.getResourceStream( resource );
    }

    public boolean isSourceModified( Resource resource )
    {
        return false;
    }
}
