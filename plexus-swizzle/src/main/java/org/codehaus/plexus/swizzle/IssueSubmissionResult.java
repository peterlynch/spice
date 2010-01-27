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

public class IssueSubmissionResult
{
    private String issueUrl;
    private String key;

    public IssueSubmissionResult( String issueUrl, String key )
    {
        this.issueUrl = issueUrl;
        this.key = key;
    }

    public String getIssueUrl()
    {
        return issueUrl;
    }

    public void setIssueUrl( String issueUrl )
    {
        this.issueUrl = issueUrl;
    }        
    
    public String getKey()
    {
        return key;
    }
    
    public void setKey( String key )
    {
        this.key = key;
    }
}
