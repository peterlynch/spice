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

