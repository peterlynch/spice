/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.components.assembly;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.assembly.filter.ContainerDescriptorHandler;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.components.io.fileselectors.FileInfo;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Components XML file filter.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 *
 * @plexus.component role="org.apache.maven.plugin.assembly.filter.ContainerDescriptorHandler" role-hint="plexus-fixed"
 */
public class ComponentsXmlArchiverFileFilter
    implements ContainerDescriptorHandler, LogEnabled
{
    // [jdcasey] Switched visibility to protected to allow testing. Also, because this class isn't final, it should allow
    // some minimal access to the components accumulated for extending classes.
    protected Map components;

    private boolean excludeOverride = false;

    public static final String COMPONENTS_XML_PATH = "META-INF/plexus/components.xml";

    private Logger logger;

    public ComponentsXmlArchiverFileFilter()
    {
        // used for plexus init.
    }

    protected ComponentsXmlArchiverFileFilter( Logger logger )
    {
        // used for testing.
        this.logger = logger;
    }

    protected void addComponentsXml( Reader componentsReader )
        throws XmlPullParserException, IOException
    {
        Xpp3Dom newDom = Xpp3DomBuilder.build( componentsReader );

        if ( newDom != null )
        {
            newDom = newDom.getChild( "components" );
        }

        if ( newDom != null )
        {
            Xpp3Dom[] children = newDom.getChildren();

            for ( int i = 0; i < children.length; i++ )
            {
                Xpp3Dom component = children[i];

                if ( components == null )
                {
                    components = new LinkedHashMap();
                }

                String role = component.getChild( "role" ).getValue();
                Xpp3Dom child = component.getChild( "role-hint" );
                String roleHint = child != null ? child.getValue() : "";

                String key = role + roleHint;
                if ( !components.containsKey( key ) )
                {
                    logger.debug( "Adding " + key );
                    components.put( key, component );
                }
                else
                {
                    logger.debug( "Component: " + key + " is already defined. Skipping." );
                }
            }
        }
    }

//    public void addComponentsXml( File componentsXml )
//        throws IOException, XmlPullParserException
//    {
//        FileReader fileReader = null;
//        try
//        {
//            fileReader = new FileReader( componentsXml );
//
//            addComponentsXml( fileReader );
//        }
//        finally
//        {
//            IOUtil.close( fileReader );
//        }
//    }

    private void addToArchive( Archiver archiver )
        throws IOException, ArchiverException
    {
        if ( components != null )
        {
            File f = File.createTempFile( "maven-assembly-plugin", "tmp" );
            f.deleteOnExit();

            // TODO use WriterFactory.newXmlWriter() when plexus-utils is upgraded to 1.4.5+
            Writer fileWriter = new OutputStreamWriter( new FileOutputStream( f ), "UTF-8" );
            try
            {
                Xpp3Dom dom = new Xpp3Dom( "component-set" );
                Xpp3Dom componentDom = new Xpp3Dom( "components" );
                dom.addChild( componentDom );

                for ( Iterator i = components.values().iterator(); i.hasNext(); )
                {
                    Xpp3Dom component = (Xpp3Dom) i.next();
                    componentDom.addChild( component );
                }

                Xpp3DomWriter.write( fileWriter, dom );
            }
            finally
            {
                IOUtil.close( fileWriter );
            }

            excludeOverride = true;

            archiver.addFile( f, COMPONENTS_XML_PATH );

            excludeOverride = false;
        }
    }

    public void finalizeArchiveCreation( Archiver archiver )
        throws ArchiverException
    {
        // this will prompt the isSelected() call, below, for all resources added to the archive.
        // FIXME: This needs to be corrected in the AbstractArchiver, where
        // runArchiveFinalizers() is called before regular resources are added...
        // which is done because the manifest needs to be added first, and the
        // manifest-creation component is a finalizer in the assembly plugin...
        for ( ResourceIterator it = archiver.getResources(); it.hasNext(); )
        {
            it.next();
        }

        try
        {
            addToArchive( archiver );
        }
        catch ( IOException e )
        {
            throw new ArchiverException( "Error finalizing component-set for archive. Reason: " + e.getMessage(), e );
        }
    }

    public List getVirtualFiles()
    {
        if ( ( components != null ) && !components.isEmpty() )
        {
            return Collections.singletonList( COMPONENTS_XML_PATH );
        }

        return null;
    }

    public boolean isSelected( FileInfo fileInfo )
        throws IOException
    {
        if ( fileInfo.isFile() )
        {
            if ( excludeOverride )
            {
                return true;
            }

            String entry = fileInfo.getName();

            if ( entry.startsWith( "/" ) )
            {
                entry = entry.substring( 1 );
            }

            if ( ComponentsXmlArchiverFileFilter.COMPONENTS_XML_PATH.equals( entry ) )
            {
                InputStream stream = null;
                InputStreamReader reader = null;

                try
                {
                    stream = fileInfo.getContents();
                    // TODO use ReaderFactory.newXmlReader() when plexus-utils is upgraded to 1.4.5+
                    reader = new InputStreamReader( stream, "UTF-8" );
                    addComponentsXml( reader );
                }
                catch ( XmlPullParserException e )
                {
                    IOException error = new IOException( "Error finalizing component-set for archive. Reason: " + e.getMessage() );
                    error.initCause( e );

                    throw error;
                }
                finally
                {
                    IOUtil.close( stream );
                    IOUtil.close( reader );
                }

                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return true;
        }
    }

    public void finalizeArchiveExtraction( UnArchiver unarchiver )
        throws ArchiverException
    {
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

}
