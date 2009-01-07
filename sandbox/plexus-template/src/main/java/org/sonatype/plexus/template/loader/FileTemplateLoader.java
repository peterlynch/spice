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
package org.sonatype.plexus.template.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.codehaus.plexus.util.StringInputStream;
import org.sonatype.plexus.template.TemplateLoader;

public class FileTemplateLoader
    implements TemplateLoader
{    
    public synchronized InputStream getResourceStream( String name )
        throws ResourceNotFoundException
    {
        try
        {
            return new FileInputStream( new File( name ) );
        }
        catch ( FileNotFoundException e )
        {
            return null;
        }        
    }    
}

