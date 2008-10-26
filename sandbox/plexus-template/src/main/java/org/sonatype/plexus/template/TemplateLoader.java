package org.sonatype.plexus.template;

import java.io.InputStream;

public interface TemplateLoader
{
    public InputStream getResourceStream( String resource );
}
