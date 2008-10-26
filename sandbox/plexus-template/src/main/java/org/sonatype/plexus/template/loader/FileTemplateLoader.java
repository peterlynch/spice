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

