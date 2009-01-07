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

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.plexus.classworlds.model.ClassworldsAppConfiguration;
import org.sonatype.plexus.classworlds.model.ClassworldsRealmConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class ClassworldsConfWriterTest
    extends TestCase
{

    @SuppressWarnings("unchecked")
    public void testWriteBasicConfig()
        throws IOException, ClassworldsIOException
    {
        Map<String, String> props = new HashMap<String, String>();
        props.put( "basedir", new File( "." ).getCanonicalPath() );

        ClassworldsAppConfiguration app = new ClassworldsAppConfiguration();
        app.setSystemProperties( props );

        app.setMainClass( "org.myco.Main" );

        ClassworldsRealmConfiguration realm = new ClassworldsRealmConfiguration( "root" );
        realm.addLoadPattern( "/path/to/lib/*.jar" );

        app.setMainRealm( realm.getRealmId() );

        app.addRealmConfiguration( realm );

        File target = File.createTempFile( "classworlds.", ".conf" );
        target.deleteOnExit();

        new ClassworldsConfWriter().write( target, app );

        StringWriter writer = new StringWriter();
        FileReader reader = null;
        try
        {
            reader = new FileReader( target );
            IOUtil.copy( reader, writer );
        }
        finally
        {
            IOUtil.close( reader );
        }

        System.out.println( writer.toString() );
    }

    @SuppressWarnings("unchecked")
    public void testWriteConfigWithTwoRealms()
        throws IOException, ClassworldsIOException
    {
        Map<String, String> props = new HashMap<String, String>();
        props.put( "basedir", new File( "." ).getCanonicalPath() );

        ClassworldsAppConfiguration app = new ClassworldsAppConfiguration();
        app.setSystemProperties( props );

        app.setMainClass( "org.myco.Main" );

        ClassworldsRealmConfiguration realm = new ClassworldsRealmConfiguration( "root" );
        realm.addLoadPattern( "/path/to/lib/*.jar" );

        app.setMainRealm( realm.getRealmId() );

        app.addRealmConfiguration( realm );

        ClassworldsRealmConfiguration sub = new ClassworldsRealmConfiguration( "sub" );
        sub.setParentRealm( realm.getRealmId() );

        sub.addImport( "org.myco.myapp.*", realm.getId() );
        sub.addLoadPattern( "/some/other/lib/path/conf" );

        app.addRealmConfiguration( sub );

        File target = File.createTempFile( "classworlds.", ".conf" );
        target.deleteOnExit();

        new ClassworldsConfWriter().write( target, app );

        StringWriter writer = new StringWriter();
        FileReader reader = null;
        try
        {
            reader = new FileReader( target );
            IOUtil.copy( reader, writer );
        }
        finally
        {
            IOUtil.close( reader );
        }

        System.out.println( writer.toString() );
    }

}
