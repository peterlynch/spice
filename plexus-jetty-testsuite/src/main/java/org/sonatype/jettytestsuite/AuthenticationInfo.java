/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package org.sonatype.jettytestsuite;

/**
 * The Class AuthenticationInfo.
 * 
 * @author cstamas
 */
public class AuthenticationInfo
{

    /** The auth method. */
    private String authMethod;

    private String credentialsFilePath;

    public String getAuthMethod()
    {
        return authMethod;
    }

    public void setAuthMethod( String authMethod )
    {
        this.authMethod = authMethod;
    }

    public String getCredentialsFilePath()
    {
        return credentialsFilePath;
    }

    public void setCredentialsFilePath( String credentialsFile )
    {
        this.credentialsFilePath = credentialsFile;
    }

}
