package org.sonatype.micromailer.imp;

import org.sonatype.micromailer.MailType;

/**
 * The html mail type.
 * 
 * @plexus.component role-hint="html"
 */
public class HtmlMailType
    extends DefaultMailType
    implements MailType
{
    public static final String HTML_TYPE_ID = "html";
    
    public HtmlMailType()
    {
        super();
        setBodyIsHtml( true );
    }
}
