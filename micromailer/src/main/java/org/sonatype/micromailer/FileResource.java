package org.sonatype.micromailer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * A Resource backed by file.
 * 
 * @author cstamas
 */
public class FileResource
    implements DataSource
{
    private final File file;

    private final String contentType;

    public FileResource( File file, String contentType )
    {
        super();

        this.file = file;

        this.contentType = contentType;
    }

    public String getName()
    {
        return file.getName();
    }

    public String getContentType()
    {
        return contentType;
    }

    public InputStream getInputStream()
        throws FileNotFoundException
    {
        return new FileInputStream( file );
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        return new FileOutputStream( file );
    }
}
