package org.sonatype.plexus.template;

public class TemplateNotFoundException
    extends Exception
{
    public TemplateNotFoundException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public TemplateNotFoundException( String message )
    {
        super( message );
    }

    public TemplateNotFoundException( Throwable cause )
    {
        super( cause );
    }
}
