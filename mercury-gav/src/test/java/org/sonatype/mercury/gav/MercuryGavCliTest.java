/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.sonatype.mercury.gav;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.mercury.util.FileUtil;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.plexus.webcontainer.ServletContainer;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryGavCliTest
    extends PlexusTestCase
{
    File _jar = new File("./target/mercury-gav-test-tests.jar");
    
    File _lr;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        _jar = new File("./target/mercury-gav-test-tests.jar");

        _lr = new File( "./target/local-repo" );
        FileUtil.delete( _lr );
        _lr.mkdirs();

        File rr = new File( "./target/remote-repo/a/a/1" );
        FileUtil.delete( rr );
        rr.mkdirs();
        
        FileUtil.copy( _jar, new File( rr, "a-1.jar" ), true );
        File pom = new File( "./src/test/resources/a-1.pom" );
        FileUtil.copy( pom, new File(rr,"a-1.pom"), true );
        
        lookup( ServletContainer.class );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testMain()
    throws Exception
    {
        String test = "this_did_work_fine";
        
        ByteArrayOutputStream os = new ByteArrayOutputStream(512);
        
        PrintStream out = System.out;
        
        System.setOut( new PrintStream(os) );
        
        MercuryGavCli.main( new String [] {
              "-s "+new File("src/test/resources/settings.xml").getCanonicalPath()
            , "a:a:1", "org.sonatype.mercury.gav.MercuryGavCliTest"
            , "1", "2", "3", test
                                          }
                          );
        
        System.setOut( out );
        
        String oss = os.toString();
        
        assertNotNull( oss );
        
        int ind = oss.indexOf( "3: "+test );
        
        assertTrue( ind > 1);
        
    }
    
    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
        try 
        {
            URL url = new File( getBasedir(), "src/main/plexus/plexus.xml" ).toURI().toURL();
            containerConfiguration.setContainerConfigurationURL( url );
        } 
        catch (MalformedURLException e) 
        {
            // do nothing
        }
    }
    
    public static void main( String[] args ) throws IOException
    {
        int count = 0;
        
        if( args == null )
            System.out.println("no params supplied");
        else
            for( String a : args )
                System.out.println((count++)+": "+a);
    }

}