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
package org.sonatype.plexus.classworlds.io;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.plexus.classworlds.model.ClassworldsAppConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class ClassworldsConfWriter
{

    public static final String CLASSWORLDS_CONF_TEMPLATE = "/classworlds.conf.vm";

    public void write( File target,
                       ClassworldsAppConfiguration config,
                       Properties velocityProperties,
                       Map<String, Object> context )
        throws ClassworldsIOException
    {
        VelocityEngine engine = new VelocityEngine();
        try
        {
            checkLogChute( velocityProperties );
            engine.init( velocityProperties );
        }
        catch ( Exception e )
        {
            throw new ClassworldsIOException( "Error initializing Velocity: " + e.getMessage(), e );
        }

        _write( target, config, context, engine );
    }

    public void write( File target,
                       ClassworldsAppConfiguration config,
                       Map<String, Object> context )
        throws ClassworldsIOException
    {
        VelocityEngine engine = new VelocityEngine();
        try
        {
            Properties props = getDefaultVelocityProperties();
            
            checkLogChute( props );
            engine.init( props );
        }
        catch ( Exception e )
        {
            throw new ClassworldsIOException( "Error initializing Velocity: " + e.getMessage(), e );
        }

        _write( target, config, context, engine );
    }

    public void write( File target,
                       ClassworldsAppConfiguration config,
                       File velocityProperties,
                       Map<String, Object> context )
        throws ClassworldsIOException
    {
        VelocityEngine engine = new VelocityEngine();
        try
        {
            FileInputStream stream = null;
            Properties props = new Properties();
            try
            {
                stream = new FileInputStream( velocityProperties );
                props.load( stream );
            }
            finally
            {
                IOUtil.close( stream );
            }
            
            checkLogChute( props );
            
            engine.init( props );
        }
        catch ( Exception e )
        {
            throw new ClassworldsIOException( "Error initializing Velocity: " + e.getMessage(), e );
        }

        _write( target, config, context, engine );
    }

    public void write( File target,
                       ClassworldsAppConfiguration config,
                       Properties velocityProperties )
        throws ClassworldsIOException
    {
        VelocityEngine engine = new VelocityEngine();
        try
        {
            checkLogChute( velocityProperties );
            engine.init( velocityProperties );
        }
        catch ( Exception e )
        {
            throw new ClassworldsIOException( "Error initializing Velocity: " + e.getMessage(), e );
        }

        _write( target, config, null, engine );
    }

    public void write( File target,
                       ClassworldsAppConfiguration config )
        throws ClassworldsIOException
    {
        VelocityEngine engine = new VelocityEngine();
        try
        {
            Properties props = getDefaultVelocityProperties();
            
            checkLogChute( props );
            engine.init( props );
        }
        catch ( Exception e )
        {
            throw new ClassworldsIOException( "Error initializing Velocity: " + e.getMessage(), e );
        }

        _write( target, config, null, engine );
    }

    private void checkLogChute( Properties props )
    {
        if ( !VelocityLogChute.hasPlexusLogger() )
        {
            VelocityLogChute.setPlexusLogger( new ConsoleLogger( Logger.LEVEL_INFO, "classworlds-writer-internal" ) );
        }
        
        if ( !props.containsKey( "runtime.log.logsystem.class" ) )
        {
            props.setProperty( "runtime.log.logsystem.class", VelocityLogChute.class.getName() );
        }
    }

    public Properties getDefaultVelocityProperties()
    {
        Properties velocityProperties = new Properties();

        velocityProperties.setProperty( "resource.loader", "class" );
        velocityProperties.setProperty( "class.resource.loader.class",
                                        ClasspathResourceLoader.class.getName() );

        return velocityProperties;
    }

    public void write( File target,
                       ClassworldsAppConfiguration config,
                       File velocityProperties )
        throws ClassworldsIOException
    {
        VelocityEngine engine = new VelocityEngine();
        try
        {
            Velocity.init( velocityProperties.getAbsolutePath() );
        }
        catch ( Exception e )
        {
            throw new ClassworldsIOException( "Error initializing Velocity: " + e.getMessage(), e );
        }

        _write( target, config, null, engine );
    }

    private void _write( File target,
                         ClassworldsAppConfiguration config,
                         Map<String, Object> context,
                         VelocityEngine engine )
        throws ClassworldsIOException
    {
        Template template;
        try
        {
            template = engine.getTemplate( CLASSWORLDS_CONF_TEMPLATE );
        }
        catch ( ResourceNotFoundException e )
        {
            throw new ClassworldsIOException( "Cannot find " + CLASSWORLDS_CONF_TEMPLATE
                                              + " template: " + e.getMessage(), e );
        }
        catch ( ParseErrorException e )
        {
            throw new ClassworldsIOException( "Error reading " + CLASSWORLDS_CONF_TEMPLATE
                                              + " template: " + e.getMessage(), e );
        }
        catch ( Exception e )
        {
            throw new ClassworldsIOException( "Error loading " + CLASSWORLDS_CONF_TEMPLATE
                                              + " template: " + e.getMessage(), e );
        }

        Context ctx = context == null ? new VelocityContext() : new VelocityContext( context );
        ctx.put( "config", config );

        FileWriter writer = null;
        try
        {
            target.getParentFile().mkdirs();
            writer = new FileWriter( target );

            template.merge( ctx, writer );
        }
        catch ( IOException e )
        {
            throw new ClassworldsIOException( "Error writing classworlds application config to: "
                                              + target, e );
        }
        finally
        {
            IOUtil.close( writer );
        }

    }

}
