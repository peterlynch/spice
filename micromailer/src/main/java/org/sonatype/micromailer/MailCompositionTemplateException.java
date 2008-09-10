package org.sonatype.micromailer;

/**
 * Thrown in case templating is unsuccesful during mail composition.
 * 
 * @author cstamas
 */
public class MailCompositionTemplateException
    extends MailCompositionException
{
    private static final long serialVersionUID = -6035479489862032914L;

    public MailCompositionTemplateException( String msg )
    {
        super( msg );
    }

    public MailCompositionTemplateException( String msg, Throwable ex )
    {
        super( msg, ex );
    }
}
