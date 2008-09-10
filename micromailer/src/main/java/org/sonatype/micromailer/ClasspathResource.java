package org.sonatype.micromailer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * A Resource that takes it's content from classpath.
 * 
 * @author cstamas
 */
public class ClasspathResource
    implements DataSource
{
    private final String path;

    private final String name;

    private final String contentType;

    public ClasspathResource( String path, String name, String contentType )
    {
        super();

        this.path = path;

        this.name = name;

        this.contentType = contentType;
    }

    public String getPath()
    {
        return path;
    }

    public String getName()
    {
        return name;
    }

    public String getContentType()
    {
        return contentType;
    }

    public InputStream getInputStream()
        throws IOException
    {
        return this.getClass().getResourceAsStream( getPath() );
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        throw new UnsupportedOperationException( "Classpath resource is not writable!" );
    }

}
