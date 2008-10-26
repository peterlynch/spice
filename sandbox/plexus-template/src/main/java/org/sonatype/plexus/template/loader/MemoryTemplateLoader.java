package org.sonatype.plexus.template.loader;

import java.io.InputStream;
import java.util.Map;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.codehaus.plexus.util.StringInputStream;
import org.sonatype.plexus.template.TemplateLoader;

public class MemoryTemplateLoader
    implements TemplateLoader
{
    private Map templates;
    
    public MemoryTemplateLoader( Map templates )
    {
        this.templates = templates;
    }
    
    public synchronized InputStream getResourceStream( String name )
        throws ResourceNotFoundException
    {
        String template = (String) templates.get( name );
        
        if ( template == null )
        {
            return null;
        }
        
        return new StringInputStream( template );
    }    
}

