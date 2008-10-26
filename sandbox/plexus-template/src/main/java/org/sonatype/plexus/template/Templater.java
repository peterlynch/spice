package org.sonatype.plexus.template;

import java.io.Writer;
import java.util.Map;

public interface Templater
{
    void setTemplateLoader( TemplateLoader memoryTemplateLoader );

    void renderTemplate( String template, Map context, Writer writer )
        throws TemplateNotFoundException, TemplateParsingException, TemplateRenderingException;
}
