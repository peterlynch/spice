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

package org.sonatype.mercury.mp3.api.cd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.sonatype.mercury.mp3.api.cd.io.xpp3.VersionListXpp3Writer;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class CdUtilTest
    extends TestCase
{
    
    String _gav = "org.apache.maven:maven-cd:3.0-alpha-1";
    AvailableVersions _versions;

    protected void setUp()
        throws Exception
    {
        _versions = new AvailableVersions();
        Scope s = new Scope();
        s.setName( "default" );
        Version v = new Version();
        v.setName( _gav );
        s.addVersion( v );
        _versions.addScope( s );
    }

    protected void tearDown()
    throws Exception
    {
    }
    
    
    public void testAvailableVersions()
    throws Exception
    {
        File f = new File("./target/ver.ver");
        new VersionListXpp3Writer().write( new FileWriter(f), _versions );
    }

}
