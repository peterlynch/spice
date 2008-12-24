package org.codehaus.plexus.swizzle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

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

@Component(role=IssueSubmitter.class, hint="jira" )
public class JiraIssueSubmitter
    implements IssueSubmitter, Initializable
{
    /* This is just what JIRA seems to use */
    private static String FILE_ATTATCHMENT_PARAMETER = "filename.1";
    
    /** Connection to the JIRA instance provided by Swizzle */
    private Jira jira;
    
    /** JIRA server URL to connect to. */
    private String serverUrl = "http://jira.codehaus.org";

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
        Issue i = jira.getIssue( "MNGECLIPSE-1110" );                
        IssueType type = jira.getIssueType( 2 );
        Priority priority = jira.getPriority( 1 );
        
        Issue issue = new Issue();                   
        issue.setProject( i.getProject() );
        issue.setAssignee( jira.getUser( request.getAssignee() ) );
        issue.setDescription( request.getDescription() );
        issue.setReporter( jira.getUser( request.getReporter() ) );
        issue.setSummary( request.getSummary() );
        issue.setType( type );
        issue.setPriority( priority );        
        
        Issue addedIssue;
        
        try
        {
            addedIssue = jira.createIssue(issue);
        }
        catch ( Exception e )
        {
            throw new IssueSubmissionException( "Error creating issue: ", e );
        }
        
        String key = addedIssue.getKey();
        System.out.println( key );
        
        if ( request.getProblemReportBundle() != null )
        {
            attachProblemReport( addedIssue.getId(), request.getProblemReportBundle() );
        }
    }

    public void submitProblemReportForIssue( String issueKey, File bundle )
        throws IssueSubmissionException
    {
    }

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
        
        // Attachment support
        // curl 'http://localhost:8080/secure/AttachFile.jspa?id=10000&os_username=test&os_password=test' -F filename.1=@small.txt
        // The issue ID (10000) can be discovered by looking at the URLs for operations on the JIRA issue (comment, assign etc).
        String uploadUrl = serverUrl + "/secure/AttachFile.jspa?id=" + issueId + "&os_username=" + username + "&os_password=" + password;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout( 8000 );
        PostMethod upload = new PostMethod( uploadUrl );

        try
        {
            Part[] parts = { new FilePart( FILE_ATTATCHMENT_PARAMETER, bundle ) };
            upload.setRequestEntity( new MultipartRequestEntity( parts, upload.getParams() ) );
            int status = client.executeMethod( upload );
            System.out.println( "statusLine>>> " + upload.getStatusLine() );

            if ( status == HttpStatus.SC_OK )
            {
                // Jira seems to return a 302 Move temporarily
                System.out.println( "OK!" );
            }
            else
            {
                System.out.println( "Upload NO!" );
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
            jira = new Jira( serverUrl + "/rpc/xmlrpc" );
            jira.login( authenticationSource.getLogin(), authenticationSource.getPassword() );
        }
        catch ( MalformedURLException e )
        {
            throw new InitializationException( "The URL '" + serverUrl + "' is not valid." );
        }
        catch ( Exception e )
        {
            throw new InitializationException( "The username and password combination is invalid." );
        }
    }
}
