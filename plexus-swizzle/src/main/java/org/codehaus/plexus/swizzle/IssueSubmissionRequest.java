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
import java.util.ArrayList;
import java.util.List;

import org.sonatype.spice.utils.proxyserver.ProxyServerConfigurator;

public class IssueSubmissionRequest
{
    private String projectId;

    private String summary;

    private String description;

    private String reporter;

    private String assignee;

    private List<File> problemReportBundles = new ArrayList<File>();

    private String environment;

    private String component;
    
    private ProxyServerConfigurator proxyConfigurator;

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

    public List<File> getProblemReportBundles()
    {
        return problemReportBundles;
    }

    public void addProblemReportBundle( File problemReportBundle )
    {
        if ( problemReportBundle == null )
        {
            throw new IllegalArgumentException( "bundle file not specified" );
        }
        problemReportBundles.add( problemReportBundle );
    }

    @Deprecated
    public File getProblemReportBundle()
    {
        return problemReportBundles.isEmpty() ? null : problemReportBundles.get( 0 );
    }

    public void setProblemReportBundle( File problemReportBundle )
    {
        this.problemReportBundles.clear();
        if ( problemReportBundle != null )
        {
            addProblemReportBundle( problemReportBundle );
        }
    }

    public void setEnvironment( String environment )
    {
        this.environment = environment;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public String getComponent()
    {
        return component;
    }

    public void setComponent( String component )
    {
        this.component = component;
    }

    public ProxyServerConfigurator getProxyConfigurator()
    {
        return proxyConfigurator;
    }
    
    public void setProxyConfigurator( ProxyServerConfigurator proxyConfigurator )
    {
        this.proxyConfigurator = proxyConfigurator;
    }
}
