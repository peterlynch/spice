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
package org.codehaus.plexus.swizzle;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.velocity.VelocityContext;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.Project;
import org.codehaus.swizzle.jira.Version;

/**
 * Author: John Tolentino
 */
public class ReleaseReportConfiguration
{
    
    public static final String RESOLVED_ISSUES_TEMPLATE = "org/codehaus/plexus/swizzle/ResolvedIssues.vm";
    public static final String VOTES_TEMPLATE = "org/codehaus/plexus/swizzle/Votes.vm";
    public static final String XDOC_SECTION_TEMPLATE = "org/codehaus/plexus/swizzle/XdocSection.vm";
    public static final String RELEASE_TEMPLATE = "org/codehaus/plexus/swizzle/Release.vm";

    public static final String RESOLVED_ISSUES = "RESOLVED_ISSUES";
    public static final String VOTES = "VOTES";
    public static final String XDOC_SECTION = "XDOC_SECTION";
    public static final String RELEASE = "RELEASE";    
    
    private String username;

    private String password;

    private String projectKey;

    private String projectVersion;

    private String jiraServerUrl;

    private String template;

    private boolean isReleaseInfoNeeded = false;

    private String groupId;

    private String artifactId;

    private String scmConnection;

    private String scmRevisionId;

    private String downloadUrl;

    private String stagingSiteUrl;

    private boolean docckPassed;

    private File docckResultDetails;

    private boolean licenseCheckPassed;

    private File licenseCheckResultDetails;

    private String dateFormat = "yyyy/MM/dd hh:mm:ss";


    private static final String EMPTY_STRING = "";

    private static final String SVN = "svn";

    private static HashMap AVAILABLE_TEMPLATES = null;

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public void setProjectKey( String projectKey )
    {
        this.projectKey = projectKey;
    }

    public String getProjectVersion()
    {
        return projectVersion;
    }

    public void setProjectVersion( String projectVersion )
    {
        this.projectVersion = projectVersion;
    }

    public String getJiraServerUrl()
    {
        return jiraServerUrl;
    }

    public void setJiraServerUrl( String jiraServerUrl )
    {
        this.jiraServerUrl = jiraServerUrl;
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate( String template )
        throws ReportConfigurationException
    {
        if ( null == AVAILABLE_TEMPLATES )
        {
            loadTemplates();
        }

        if ( EMPTY_STRING.equals( template ) )
        {
            String exceptionString = new String();
            throw new ReportConfigurationException( "Provided template was an empty string. Was expecting " +
                getTemplateKeys( AVAILABLE_TEMPLATES.keySet() ) + "or a fully qualified path to a user-provided " +
                "velocity template." );
        }

        if ( AVAILABLE_TEMPLATES.containsKey( template ) )
        {
            this.template = (String) AVAILABLE_TEMPLATES.get( template );
        }
        else
        {
            this.template = template;
        }
    }

    private void loadTemplates()
    {
        AVAILABLE_TEMPLATES = new HashMap();
        AVAILABLE_TEMPLATES.put( RESOLVED_ISSUES, RESOLVED_ISSUES_TEMPLATE );
        AVAILABLE_TEMPLATES.put( VOTES, VOTES_TEMPLATE );
        AVAILABLE_TEMPLATES.put( XDOC_SECTION, XDOC_SECTION_TEMPLATE );
        AVAILABLE_TEMPLATES.put( RELEASE, RELEASE_TEMPLATE );
    }

    public boolean isReleaseInfoNeeded()
    {
        return isReleaseInfoNeeded;
    }

    public void setReleaseInfoNeeded( boolean releaseInfoNeeded )
    {
        isReleaseInfoNeeded = releaseInfoNeeded;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getScmConnection()
    {
        return scmConnection;
    }

    public void setScmConnection( String scmConnection )
    {
        this.scmConnection = scmConnection;
    }

    public String getScmRevisionId()
    {
        return ( null == scmRevisionId ? EMPTY_STRING : scmRevisionId );
    }

    public void setScmRevisionId( String scmRevisionId )
    {
        this.scmRevisionId = scmRevisionId;
    }

    public String getScmType()
    {
        String scmType = "UNKNOWN";

        if ( scmConnection.indexOf( "scm:" + SVN + ":" ) != -1 )
        {
            scmType = SVN;
        }

        return scmType;
    }

    public String getScmUrl()
    {
        String scmUrl = scmConnection.substring( "scm:".length(), scmConnection.length() );
        scmUrl = scmUrl.substring( scmUrl.indexOf( ":" ) + 1, scmUrl.length() );
        return scmUrl;
    }

    public String getScmCheckoutCommand()
    {
        StringBuffer scmCheckoutCommand = new StringBuffer( "" );

        if ( SVN.equals( getScmType() ) )
        {
            scmCheckoutCommand.append( "svn co " );
            if ( !EMPTY_STRING.equals( getScmRevisionId() ) )
            {
                scmCheckoutCommand.append( "-r " );
                scmCheckoutCommand.append( getScmRevisionId() );
                scmCheckoutCommand.append( " " );
            }
            scmCheckoutCommand.append( getScmUrl() );
        }
        return scmCheckoutCommand.toString();
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public void setDownloadUrl( String downloadUrl )
    {
        this.downloadUrl = downloadUrl;
    }

    public String getStagingSiteUrl()
    {
        return stagingSiteUrl;
    }

    public void setStagingSiteUrl( String stagingSiteUrl )
    {
        this.stagingSiteUrl = stagingSiteUrl;
    }

    public Boolean isDocckPassed()
    {
        return new Boolean( docckPassed );
    }

    public void setDocckPassed( boolean docckPassed )
    {
        this.docckPassed = docckPassed;
    }

    public File getDocckResultDetails()
    {
        return docckResultDetails;
    }

    public void setDocckResultDetails( String docckResultDetails )
    {
        this.docckResultDetails = new File( docckResultDetails );
    }

    public void setDocckResultDetails( File docckResultDetails )
    {
        this.docckResultDetails = docckResultDetails;
    }

    public String getDocckResultContents()
    {
        return "";
    }

    public Boolean isLicenseCheckPassed()
    {
        return new Boolean( licenseCheckPassed );
    }

    public void setLicenseCheckPassed( boolean licenseCheckPassed )
    {
        this.licenseCheckPassed = licenseCheckPassed;
    }

    public File getLicenseCheckResultDetails()
    {
        return licenseCheckResultDetails;
    }

    public void setLicenseCheckResultDetails( String licenseCheckResultDetails )
    {
        this.licenseCheckResultDetails = new File( licenseCheckResultDetails );
    }

    public void setLicenseCheckResultDetails( File licenseCheckResultDetails )
    {
        this.licenseCheckResultDetails = licenseCheckResultDetails;
    }

    public String getDateFormat()
    {
        return dateFormat;
    }

    public void setDateFormat( String dateFormat )
    {
        this.dateFormat = dateFormat;
    }

    public String getLicenseCheckResultContents()
    {
        String contents;

        FileInputStream resource = null;
        try
        {
            resource = new FileInputStream( licenseCheckResultDetails );
            contents = Utils.streamToString( resource );
        }
        catch ( FileNotFoundException e )
        {
            contents = "Can't find License Header results file: " + licenseCheckResultDetails.getAbsolutePath() +
                e.getMessage();
        }
        catch ( IOException e )
        {
            contents = "Can't read License Header results file: " + licenseCheckResultDetails.getAbsolutePath() +
                e.getMessage();
        }
        return contents;
    }


    private String getTemplateKeys( Set keySet )
    {
        StringBuffer templateKeys = new StringBuffer();
        Iterator itr = keySet.iterator();
        while ( itr.hasNext() )
        {
            templateKeys.append( "\"" + itr.next() + "\", " );
        }
        return templateKeys.toString();
    }
    
    public void putStandardConfigToContext( ReleaseReportConfiguration configuration, VelocityContext context )
    {
        context.put( "username", configuration.getUsername() );
        context.put( "password", configuration.getPassword() );
        context.put( "projectKey", configuration.getProjectKey() );
        context.put( "projectVersion", configuration.getProjectVersion() );
        context.put( "jiraServerUrl", configuration.getJiraServerUrl() );
    }

    public void putReleaseInfoConfigToContext( Jira jira, ReleaseReportConfiguration configuration, VelocityContext context )
        throws Exception
    {
        context.put( "previousReleaseVersion", getPreviousRelease( jira, configuration ) );
        context.put( "lastReleaseDate", getReleaseDate( jira, configuration ) );

        context.put( "groupId", configuration.getGroupId() );
        context.put( "artifactId", configuration.getArtifactId() );
        context.put( "downloadUrl", configuration.getDownloadUrl() );
        context.put( "scmUrl", configuration.getScmUrl() );
        context.put( "scmRevisionId", configuration.getScmRevisionId() );
        context.put( "scmType", configuration.getScmType() );
        context.put( "scmCheckoutCommand", configuration.getScmCheckoutCommand() );
        context.put( "stagingSiteUrl", configuration.getStagingSiteUrl() );
        context.put( "docckPassed", configuration.isDocckPassed() );
        context.put( "docckResultDetails", configuration.getDocckResultDetails() );
        context.put( "docckResultContents", configuration.getDocckResultContents() );
        context.put( "licenseCheckPassed", configuration.isLicenseCheckPassed() );
        context.put( "licenseCheckResultDetails", configuration.getLicenseCheckResultDetails() );
        context.put( "licenseCheckResultContents", configuration.getLicenseCheckResultContents() );
    }

    private String getPreviousRelease( Jira jira, ReleaseReportConfiguration configuration )
        throws Exception
    {        
        String previousVersion = "";

        Project project = jira.getProject( configuration.getProjectKey() );

        List versions = jira.getVersions( project );

        for ( Iterator itr = versions.iterator(); itr.hasNext(); )
        {
            Version nextVersion = (Version) itr.next();
            if ( configuration.getProjectVersion().equals( nextVersion.getName() ) )
            {
                return previousVersion;
            }
            else
            {
                previousVersion = nextVersion.getName();
            }
        }

        return previousVersion;
    }

    private String getReleaseDate( Jira jira, ReleaseReportConfiguration configuration )
        throws Exception
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( configuration.getDateFormat() );


        Project project = jira.getProject( configuration.getProjectKey() );

        String previousRelease = getPreviousRelease( jira, configuration );

        Version version;

        if ( "".equals( previousRelease ) )
        {
            version = jira.getVersion( project, configuration.getProjectVersion() );
        }
        else
        {
            version = jira.getVersion( project, previousRelease );
        }

        return simpleDateFormat.format( version.getReleaseDate() );
    }
}
