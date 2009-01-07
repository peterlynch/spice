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
package org.sonatype.plexus.template;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.sonatype.plexus.template.loader.FileTemplateLoader;

/**
 * @goal render
 * @phase generate-resources
 * @author jvanzyl
 */
public class TemplaterMojo
    extends AbstractMojo
{
    /** @component */
    private Templater templater;

    /** @parameter expression="${project.properties}" */
    private Properties properties;

    /** @parameter */
    private List templates;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        System.out.println( "properties = " + properties );
                 
        templater.setTemplateLoader( new FileTemplateLoader() );

        for ( Iterator i = templates.iterator(); i.hasNext(); )
        {
            Template template = (Template) i.next();

            try
            {
                File f = new File( template.getTarget() );

                if ( !f.getParentFile().exists() )
                {
                    f.getParentFile().mkdirs();
                }
                Writer writer = new FileWriter( f );
                templater.renderTemplate( template.getSource(), properties, writer );
                IOUtil.close( writer );
                
                if ( template.getPerms() != null )
                {
                    setPerms( f, template.getPerms() );
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    private void setPerms( File file, String perms )
        throws Exception
    {
        Commandline cmd = new Commandline();
        cmd.setWorkingDirectory( file.getParentFile().getAbsolutePath() );
        cmd.setExecutable( "chmod" );
        cmd.createArgument().setValue( perms );
        cmd.createArgument().setValue( file.getName() );

        StringWriter swriter = new StringWriter();        
        Process process = cmd.execute();
        Reader reader = new InputStreamReader( process.getInputStream() );

        char[] chars = new char[16];
        int read = -1;
        while ( ( read = reader.read( chars ) ) > -1 )
        {
            swriter.write( chars, 0, read );
        }                
    }
}
