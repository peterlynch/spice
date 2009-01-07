/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.plexus.template;

import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

@Component(role = Templater.class)
public class VelocityTemplater
    extends AbstractLogEnabled
    implements Templater, Initializable, LogSystem
{
    /** The velocity engine */
    private VelocityEngine engine;

    /**
     * Properties that we always want inserted into the Velocity context before rendering the
     * template.
     */
    private Properties properties;

    public void setTemplateLoader( TemplateLoader loader )
    {
        DynamicResourceLoader.setTemplateLoader( loader );
    }

    // ----------------------------------------------------------------------
    // Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
        throws InitializationException
    {
        engine = new VelocityEngine();
        // avoid "unable to find resource 'VM_global_library.vm' in any resource loader."
        engine.setProperty( "velocimacro.library", "" );
        engine.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, this );
        engine.setProperty( "resource.loader", "dynamic" );
        engine.setProperty( "dynamic.resource.loader.class", "org.sonatype.plexus.template.DynamicResourceLoader" );

        try
        {
            engine.init();
        }
        catch ( Exception e )
        {
            throw new InitializationException( "Cannot start the velocity engine: ", e );
        }
    }

    public void renderTemplate( String template, Map context, Writer writer )
        throws TemplateNotFoundException, TemplateParsingException, TemplateRenderingException
    {
        try
        {
            Template t = engine.getTemplate( template );
            VelocityContext vc = new VelocityContext();

            if ( context != null )
            {
                for ( Iterator i = context.keySet().iterator(); i.hasNext(); )
                {
                    String key = (String) i.next();
                    vc.put( key, context.get( key ) );
                }
            }
            t.merge( vc, writer );

        }
        catch ( ResourceNotFoundException e )
        {
            throw new TemplateNotFoundException( "The template name '" + template + "' cannot be found.", e );
        }
        catch ( ParseErrorException e )
        {
            throw new TemplateParsingException( "The template name '" + template + "' cannot be parsed.", e );
        }
        catch ( Exception e )
        {
            throw new TemplateRenderingException( "The template name '" + template + "' cannot be rendered.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void logVelocityMessage( int level, String message )
    {
        switch ( level )
        {
            case LogSystem.WARN_ID:
                getLogger().warn( message );
                break;
            case LogSystem.INFO_ID:
                // velocity info messages are too verbose, just consider them as debug messages...
                getLogger().debug( message );
                break;
            case LogSystem.DEBUG_ID:
                getLogger().debug( message );
                break;
            case LogSystem.ERROR_ID:
                getLogger().error( message );
                break;
            default:
                getLogger().debug( message );
                break;
        }
    }

    public void init( RuntimeServices arg0 )
        throws Exception
    {
        // TODO Auto-generated method stub

    }
}
