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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.swizzle.IssueSubmissionResult;
import org.codehaus.plexus.swizzle.IssueSubmitter;
import org.codehaus.plexus.swizzle.jira.authentication.AuthenticationSource;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.IssueType;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.Priority;
import org.codehaus.swizzle.jira.Project;

//TODO detect whether the remote api is enabled
@Component( role = IssueSubmitter.class, hint = "jira" )
public class JiraIssueSubmitter
    implements IssueSubmitter, Initializable
{
    /* This is just what JIRA seems to use */
    private static String FILE_ATTATCHMENT_PARAMETER = "filename.1";

    /** Connection to the JIRA instance provided by Swizzle */
    private Jira jira;

    /** JIRA server URL to connect to. */
    private String serverUrl;

    @Requirement
    private AuthenticationSource authenticationSource;

    /* For plexus use */
    public JiraIssueSubmitter()
    {
    }

    public JiraIssueSubmitter( String serverUrl, AuthenticationSource authenticationSource )
        throws InitializationException
    {
        this.serverUrl = serverUrl;
        this.authenticationSource = authenticationSource;
        initialize();
    }

    public IssueSubmissionResult submitIssue( IssueSubmissionRequest request )
        throws IssueSubmissionException
    {
        Project project = jira.getProject( request.getProjectId() );
        IssueType type = jira.getIssueType( 2 );
        Priority priority = jira.getPriority( 1 );

        validateIssueSubmissionRequest( request );

        Issue issue = new Issue();
        issue.setProject( project );
        issue.setSummary( request.getSummary() );
        issue.setDescription( request.getDescription() );
        issue.setReporter( jira.getUser( request.getReporter() ) );
        if ( request.getAssignee() != null )
        {
            issue.setAssignee( jira.getUser( request.getAssignee() ) );
        }
        issue.setType( type );
        issue.setPriority( priority );
        issue.setEnvironment( request.getEnvironment() );

        if ( StringUtils.isNotEmpty( request.getComponent() ) )
        {
            issue.setComponents( Arrays.asList( jira.getComponent( project, request.getComponent() ) ) );
        }

        // We need to create an issue so that we can create an attachment. The XMLRPC API does not
        // allow for attachments so we have to use separate http client call to submit the attachment.

        Issue addedIssue;

        try
        {
            addedIssue = jira.createIssue( issue );
        }
        catch ( Exception e )
        {
            throw new IssueSubmissionException( "Error creating issue: " + e.getMessage(), e );
        }

        processAttachments( addedIssue.getId(), request, request.getProblemReportBundles() );

        processAttachments( addedIssue.getId(), request, request.getScreenCaptures() );
        
        return new IssueSubmissionResult( addedIssue.getLink(), addedIssue.getKey() );
    }

    private void validateIssueSubmissionRequest( IssueSubmissionRequest request )
        throws IssueSubmissionException
    {
        if ( !userExists( request.getReporter() ) )
        {
            throw new IssueSubmissionException( "The reporter must exist in the JIRA users database. The user '"
                + request.getAssignee() + "' does not exist." );
        }

        if ( StringUtils.isNotEmpty( request.getAssignee() ) && !userExists( request.getAssignee() ) )
        {
            throw new IssueSubmissionException( "The assignee must exist in the JIRA users database. The user '"
                + request.getAssignee() + "' does not exist." );
        }
    }

    private boolean userExists( String login )
    {
        if ( jira.getUser( login ).getName() != null )
        {
            return true;
        }

        return false;
    }

    // Attachment support is being provided by creating a direct call against the web interface. We need to use the
    // following
    // URL template:
    //
    // /secure/AttachFile.jspa?id=${issueId}&os_username=${username}&os_password=${password}

    private void processAttachments( int issueId, IssueSubmissionRequest request, List<File> attachements )
        throws IssueSubmissionException
    {
        if ( attachements == null || attachements.isEmpty() )
        {
            return;
        }

        String username = authenticationSource.getLogin();
        String password = authenticationSource.getPassword();

        String uploadUrl = serverUrl + "/secure/AttachFile.jspa?id=" + issueId + "&os_username=" + username + "&os_password=" + password;

        DefaultHttpClient client = getHttpClient( request );
        //client.getHttpConnectionManager().getParams().setConnectionTimeout( 8000 );
        
        for ( File attachment : attachements )
        {
            try
            {
                HttpPost httppost = new HttpPost( uploadUrl );  
                MultipartEntity reqEntity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE);                     
				reqEntity.addPart(FILE_ATTATCHMENT_PARAMETER, new FileBody( attachment ) );
				httppost.setEntity(reqEntity);				
				HttpResponse response = client.execute(httppost);
				HttpEntity resEntity = response.getEntity();

				if (resEntity != null) 
				{
					String page = EntityUtils.toString(resEntity);
				}            	
            }
            catch ( FileNotFoundException e )
            {
                throw new IssueSubmissionException( "The problem report bundle specified does not exist: "
                    + attachment );
            }
            catch ( IOException e )
            {
                throw new IssueSubmissionException( "There was an error posting the problem report bundle: ", e );
            }
        }
    }

    public void initialize()
        throws InitializationException
    {
        try
        {
            jira = new Jira( serverUrl );
            jira.login( authenticationSource.getLogin(), authenticationSource.getPassword() );
        }
        catch ( MalformedURLException e )
        {
            throw new InitializationException( "The URL '" + serverUrl + "' is not valid." );
        }
        catch ( Exception e )
        {
            throw new InitializationException( "The username and password combination is invalid.", e );
        }
    }

    private DefaultHttpClient getHttpClient( IssueSubmissionRequest request )
    {
        DefaultHttpClient client = new DefaultHttpClient();

        ProxyServerConfigurator configurator = request.getProxyConfigurator();

        if ( configurator == null )
        {
            configurator = new DefaultProxyServerConfigurator();
        }

        configurator.applyToClient( client );

        return client;
    }
}
