package org.sonatype.micromailer.imp;

import java.util.HashMap;
import java.util.Map;

import javax.activation.DataSource;

import org.sonatype.micromailer.MailType;

/**
 * The simple default mail type.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultMailType
    implements MailType
{
    public static final String DEFAULT_TYPE_ID = "default";

    public static final String SUBJECT_KEY = "subject";

    public static final String BODY_KEY = "body";

    private String typeId;

    private boolean bodyIsHtml;

    private boolean storeable;

    private String subjectTemplate;

    private String bodyTemplate;

    private Map<String, DataSource> inlineResources;

    public DefaultMailType()
    {
        super();

        this.typeId = "default";

        this.bodyIsHtml = false;

        this.subjectTemplate = "$" + SUBJECT_KEY;

        this.bodyTemplate = "$" + BODY_KEY;

        this.inlineResources = new HashMap<String, DataSource>();
    }

    public String getTypeId()
    {
        return typeId;
    }

    public void setTypeId( String typeId )
    {
        this.typeId = typeId;
    }

    public boolean isBodyIsHtml()
    {
        return bodyIsHtml;
    }

    public boolean isStoreable()
    {
        return storeable;
    }

    public void setStoreable( boolean storeable )
    {
        this.storeable = storeable;
    }

    public void setBodyIsHtml( boolean bodyIsHtml )
    {
        this.bodyIsHtml = bodyIsHtml;
    }

    public String getSubjectTemplate()
    {
        return subjectTemplate;
    }

    public void setSubjectTemplate( String subjectTemplate )
    {
        this.subjectTemplate = subjectTemplate;
    }

    public String getBodyTemplate()
    {
        return bodyTemplate;
    }

    public void setBodyTemplate( String bodyTemplate )
    {
        this.bodyTemplate = bodyTemplate;
    }

    public Map<String, DataSource> getInlineResources()
    {
        return inlineResources;
    }

    public void setInlineResources( Map<String, DataSource> inlineResources )
    {
        this.inlineResources = inlineResources;
    }

}
