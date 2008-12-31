package org.codehaus.plexus.swizzle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.swizzle.jira.authentication.AuthenticationSource;
import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.IssueType;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.Priority;
import org.codehaus.swizzle.jira.Project;
import org.codehaus.swizzle.jira.User;

//TODO detect whether the remote api is enabled
@Component(role=IssueSubmitter.class, hint="jira" )
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
    
    public void submitIssue( IssueSubmissionRequest request )
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
        issue.setAssignee( jira.getUser( request.getAssignee() ) );
        issue.setType( type );
        issue.setPriority( priority );         
                
         // We need to create an issue so that we can create an attachment. The XMLRPC API does not
         // allow for attachments so we have to use separate http client call to submit the attachment. 
        
        Issue addedIssue;
        
        try
        {
            addedIssue = jira.createIssue(issue);
        }
        catch ( Exception e )
        {
            throw new IssueSubmissionException( "Error creating issue: " + e.getMessage(), e );
        }
        
        if ( request.getProblemReportBundle() != null )
        {
            attachProblemReport( addedIssue.getId(), request.getProblemReportBundle() );
        }
    }

    private void validateIssueSubmissionRequest( IssueSubmissionRequest request )
        throws IssueSubmissionException
    {
        if ( !userExists( request.getReporter() ) )
        {
            throw new IssueSubmissionException( "The reporter must exist in the JIRA users database. The user '" + request.getAssignee() + "' does not exist." );
        }        

        if ( !userExists( request.getAssignee() ) )
        {
            throw new IssueSubmissionException( "The assignee must exist in the JIRA users database. The user '" + request.getAssignee() + "' does not exist." );
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
    
    // Attachment support is being provided by creating a direct call against the web interface. We need to use the following
    // URL template:
    //
    // /secure/AttachFile.jspa?id=${issueId}&os_username=${username}&os_password=${password}
    
    private void attachProblemReport( String issueKey, File bundle )
        throws IssueSubmissionException
    {
        Issue issue = jira.getIssue( issueKey );
        attachProblemReport( issue.getId(), bundle );
    }

    private void attachProblemReport( int issueId, File bundle )
        throws IssueSubmissionException
    {
        String username = authenticationSource.getLogin();
        String password = authenticationSource.getPassword();
        
        String uploadUrl = serverUrl + "/secure/AttachFile.jspa?id=" + issueId + "&os_username=" + username + "&os_password=" + password;
        
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout( 8000 );
        PostMethod upload = new PostMethod( uploadUrl );

        try
        {
            Part[] parts = { new FilePart( FILE_ATTATCHMENT_PARAMETER, bundle ) };
            upload.setRequestEntity( new MultipartRequestEntity( parts, upload.getParams() ) );
            int status = client.executeMethod( upload );

            // JIRA returns temporarily moved because the web UI moves to another page when the attachment
            // submission is done normally.
            
            if ( status != HttpStatus.SC_MOVED_TEMPORARILY )
            {
                // This should not fail once we have successfully created the issue, but in the event the
                // attachment does fail we should probably roll back the creation of the issue.
            }

            upload.releaseConnection();
        }
        catch ( FileNotFoundException e )
        {
            throw new IssueSubmissionException( "The problem report bundle specified does not exist: " + bundle );
        }
        catch ( HttpException e )
        {
            throw new IssueSubmissionException( "There was an error posting the problem report bundle: ", e );
        }
        catch ( IOException e )
        {
            throw new IssueSubmissionException( "There was an error posting the problem report bundle: ", e );
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
}
