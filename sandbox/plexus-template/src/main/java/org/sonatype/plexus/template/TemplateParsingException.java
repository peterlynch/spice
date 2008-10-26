package org.sonatype.plexus.template;

public class TemplateParsingException
    extends Exception
{
    public TemplateParsingException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public TemplateParsingException( String message )
    {
        super( message );
    }

    public TemplateParsingException( Throwable cause )
    {
        super( cause );
    }
}
