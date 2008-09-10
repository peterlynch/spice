package org.sonatype.micromailer.imp;

import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.sonatype.micromailer.EmailerConfiguration;
import org.sonatype.micromailer.MailComposer;
import org.sonatype.micromailer.MailCompositionAttachmentException;
import org.sonatype.micromailer.MailCompositionMessagingException;
import org.sonatype.micromailer.MailCompositionTemplateException;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailType;

/**
 * The Velocity powered mail composer.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultMailComposer
    implements MailComposer
{
    /**
     * @plexus.requirement
     */
    private VelocityComponent velocityComponent;

    protected Map<String, Object> initialVelocityContext;

    public Map<String, Object> getInitialVelocityContext()
    {
        return initialVelocityContext;
    }

    public void setInitialVelocityContext( Map<String, Object> initialVelocityContext )
    {
        this.initialVelocityContext = initialVelocityContext;
    }

    // ====================
    // mail composer iface

    public void composeMail( EmailerConfiguration configuration, MailRequest request, MailType mailType )
        throws MailCompositionTemplateException,
            MailCompositionAttachmentException,
            MailCompositionMessagingException
    {
        if ( request.getExpandedSubject() == null )
        {
            request.setExpandedSubject( expandTemplateFromString( mailType.getSubjectTemplate(), request
                .getBodyContext() ) );
        }

        if ( request.getExpandedBody() == null )
        {
            request
                .setExpandedBody( expandTemplateFromString( mailType.getBodyTemplate(), request.getBodyContext() ) );
        }
    }

    protected String expandTemplateFromString( String template, Map<String, Object> model )
        throws MailCompositionTemplateException
    {
        try
        {
            StringWriter sw = new StringWriter();

            VelocityContext context = new VelocityContext( getInitialVelocityContext(), new VelocityContext( model ) );

            velocityComponent.getEngine().evaluate( context, sw, "SUBJECT", template );

            return sw.toString();
        }
        catch ( Exception ex )
        {
            throw new MailCompositionTemplateException( "Velocity throw exception during template merge.", ex );
        }
    }

    protected String expandTemplateFromResource( String templateResourceName, Map<String, Object> model )
        throws MailCompositionTemplateException
    {
        try
        {
            StringWriter sw = new StringWriter();

            Template template = velocityComponent.getEngine().getTemplate( templateResourceName, "UTF-8" );

            VelocityContext context = new VelocityContext( getInitialVelocityContext(), new VelocityContext( model ) );

            template.merge( context, sw );

            return sw.toString();
        }
        catch ( Exception ex )
        {
            throw new MailCompositionTemplateException( "Velocity throw exception during template merge.", ex );
        }
    }

}
