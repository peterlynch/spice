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
package org.codehaus.plexus.swizzle;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jirareport.Main;

@Component( role = JiraReporter.class )
public class DefaultJiraReporter
    implements JiraReporter
{

    private Jira jira;
    private VelocityContext context;
    private String jiraServerUrl = "http://jira.codehaus.org";
    private String username = "xxx";
    private String password = "xxx";

    public void generateReport( String template, Map configuration, OutputStream os )
        throws Exception
    {
        context = new VelocityContext();
        context.put( "jira", getJira() );
        context.put( "jiraServerUrl", jiraServerUrl );
        context.put( "username", username );
        context.put( "password", password );
        context.put( "stringtool", new StringTool() );

        for ( Iterator i = configuration.keySet().iterator(); i.hasNext(); )
        {
            String key = (String) i.next();
            context.put( key, configuration.get( key ) );
        }

        PrintStream ps = new PrintStream( os );
        Main.generate( context, template, ps );
        IOUtil.close( ps );

        LineNumberReader read = new LineNumberReader( new FileReader( new File( "roadmap.txt" ) ) );
        Writer w = new FileWriter( "o.txt" );

        String line;
        boolean skip = false;
        while ( ( line = read.readLine() ) != null )
        {
            if ( line.startsWith( "h1." ) )
            {
                skip = false;
            }

            if ( line.startsWith( "ERD:" ) )
            {
                skip = true;
            }

            if ( !skip )
            {
                w.write( line + "\n" );
            }
        }

        IOUtil.close( read );
        IOUtil.close( w );
    }

    private Jira getJira()
        throws Exception
    {
        if ( null == jira )
        {
            jira = new Jira( jiraServerUrl + "/rpc/xmlrpc" );
            jira.login( username, password );
        }
        return jira;
    }

    public class StringTool
    {

        public String replace( String a, String b, String c )
        {
            return c.replaceAll( a, b );
        }
    }
}
