package org.sonatype.plexus.template;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.plexus.template.loader.ClassLoaderTemplateLoader;
import org.sonatype.plexus.template.loader.FileTemplateLoader;
import org.sonatype.plexus.template.loader.MemoryTemplateLoader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DefaultTemplaterTest
    extends PlexusTestCase
{
    private Templater templater;
    private Map context;

    protected void setUp()
        throws Exception
    {
        context = new HashMap();
        context.put( "name", "jason" );
        context.put( "project", "plexus" );
        context.put( "company", "sonatype" );

        templater = (Templater) lookup( Templater.class.getName() );
    }

    /**
     * Create a set of templates in a Map and use that as the source of templates for rendering.
     */
    public void testTemplaterWithMemoryTemplateLoader()
        throws Exception
    {
        Map templates = new HashMap();
        templates.put( "template", "i am ${name}" );
        templater.setTemplateLoader( new MemoryTemplateLoader( templates ) );
        Writer writer = new StringWriter();
        templater.renderTemplate( "template", context, writer );
        assertEquals( "i am jason", writer.toString() );
    }

    public void testTemplaterWithClassLoaderTemplateLoader()
        throws Exception
    {
        templater.setTemplateLoader( new ClassLoaderTemplateLoader() );
        Writer writer = new StringWriter();
        templater.renderTemplate( "templates/classloader-template.txt", context, writer );
        assertEquals( "the project is plexus", writer.toString() );
    }

    public void testTemplaterWithFileTemplateLoader()
        throws Exception
    {
        templater.setTemplateLoader( new FileTemplateLoader() );
        Writer writer = new StringWriter();
        File template = new File( getBasedir(), "src/test/templates/file-template.txt" );
        templater.renderTemplate( template.getAbsolutePath(), context, writer );
        assertEquals( "the company is sonatype", writer.toString() );
    }
}
