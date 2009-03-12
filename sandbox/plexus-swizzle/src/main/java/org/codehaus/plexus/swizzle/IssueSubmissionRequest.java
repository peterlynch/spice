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

public class IssueSubmissionRequest
{
    private String projectId;
    private String summary;    
    private String description;
    private String reporter;
    private String assignee;
    private File problemReportBundle;
	private String environment;
    public String getProjectId()
    {
        return projectId;
    }
    public void setProjectId( String projectId )
    {
        this.projectId = projectId;
    }
    public String getSummary()
    {
        return summary;
    }
    public void setSummary( String summary )
    {
        this.summary = summary;
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription( String description )
    {
        this.description = description;
    }
    public String getReporter()
    {
        return reporter;
    }
    public void setReporter( String reporter )
    {
        this.reporter = reporter;
    }
    public String getAssignee()
    {
        return assignee;
    }
    public void setAssignee( String assignee )
    {
        this.assignee = assignee;
    }
    public File getProblemReportBundle()
    {
        return problemReportBundle;
    }
    public void setProblemReportBundle( File problemReportBundle )
    {
        this.problemReportBundle = problemReportBundle;
    }
    public void setEnvironment(String environment) {
    	this.environment = environment;
    }
	public String getEnvironment() {
		return environment;
	}

    
}
