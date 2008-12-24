/**
 * 
 * Copyright 2006
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.codehaus.plexus.swizzle;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.swizzle.jira.authentication.PropertiesFileAuthenticationSource;

/**
 * @author jtolentino
 * @version $$Id$$
 */
public class JiraReporterTest
    extends PlexusTestCase
{
    public void testReporting()
        throws Exception
    {
        /*
        JiraReporter reporter = (JiraReporter) lookup( JiraReporter.class );        
        Map m = new HashMap();
        m.put( "projectKey", "NXCM" );
        m.put( "projectVersion", "1.3eet" );
        File file = new File( getBasedir(), "roadmap.txt" );
        OutputStream result = new FileOutputStream( file );
        reporter.generateReport( "org/sonatype/plexus/report/roadmap/Roadmap.vm", m, result );        
        */
    }
    
    public void testAttachment()
        throws Exception
    {
        IssueSubmitter is = new JiraIssueSubmitter( "http://jira.codehaus.org", new PropertiesFileAuthenticationSource( null ) );
        
        IssueSubmissionRequest r = new IssueSubmissionRequest();
        r.setProjectId( "MNGECLIPSE" );
        r.setSummary( "summary" );
        r.setDescription( "description" );
        r.setAssignee( "jason" );
        r.setReporter( "jason" );
        r.setProblemReportBundle( new File( getBasedir(), "src/test/bundle.zip" ) );
        
        is.submitIssue( r );
    }
}
