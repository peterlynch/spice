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
package org.sonatype.plexus.template.loader;

import java.io.InputStream;

import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.apache.commons.collections.ExtendedProperties;
import org.sonatype.plexus.template.TemplateLoader;

public class ClassLoaderTemplateLoader
    implements TemplateLoader
{
    private ClassLoader loader;
    
    public ClassLoaderTemplateLoader()
    {
        loader = Thread.currentThread().getContextClassLoader();        
    }

    public ClassLoaderTemplateLoader( ClassLoader loader )
    {
        this.loader = loader;
    }

    public synchronized InputStream getResourceStream( String name )
        throws ResourceNotFoundException
    {
        InputStream result = null;
        
        if (name == null || name.length() == 0)
        {
            throw new ResourceNotFoundException ("No template name provided");
        }
        
        try 
        {
            result = loader.getResourceAsStream( name );
        }
        catch( Exception fnfe )
        {
            throw new ResourceNotFoundException( fnfe.getMessage() );
        }
        
        return result;
    }    
}

