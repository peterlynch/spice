package org.sonatype.plexus.template;

public class TemplateRenderingException
    extends Exception
{
    public TemplateRenderingException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public TemplateRenderingException( String message )
    {
        super( message );
    }

    public TemplateRenderingException( Throwable cause )
    {
        super( cause );
    }
}
