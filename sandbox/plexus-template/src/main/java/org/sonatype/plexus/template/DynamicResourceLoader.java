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
