package org.sonatype.micromailer;

import java.util.Map;

import javax.activation.DataSource;

/**
 * A mail type descriptor, holding all the needed stuff to have the mail assembled.
 * 
 * @author cstamas
 */
public interface MailType
{
    public String getTypeId();

    public boolean isBodyIsHtml();

    public boolean isStoreable();

    public String getSubjectTemplate();

    public String getBodyTemplate();

    public Map<String, DataSource> getInlineResources();
}
