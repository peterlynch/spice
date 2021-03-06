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

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.swizzle.jira.authentication.DefaultAuthenticationSource;
import org.codehaus.plexus.swizzle.jira.authentication.PropertiesFileAuthenticationSource;

/**
 * @author Jason van Zyl
 */
public class JiraReporterTest
    extends PlexusTestCase
{
    public void testAttachment()
        throws Exception
    {
        IssueSubmitter is = new JiraIssueSubmitter( "https://issues.sonatype.org", new DefaultAuthenticationSource( "sonatype_problem_reporting", "sonatype_problem_reporting" ) );
        
        IssueSubmissionRequest r = new IssueSubmissionRequest();
        r.setProjectId( "PR" );
        r.setSummary( "summary" );
        r.setDescription( "description" );
        r.setAssignee( "sonatype_problem_reporting" );
        r.setReporter( "sonatype_problem_reporting" );
        r.addScreenCapture( new File( getBasedir(), "src/test/capture.png" ) );
        r.setEnvironment("Eclipse 3.4.2");        
        IssueSubmissionResult result = is.submitIssue( r );
        assertNotNull( result.getIssueUrl() );
        System.out.println( result.getIssueUrl() );
    }
}
