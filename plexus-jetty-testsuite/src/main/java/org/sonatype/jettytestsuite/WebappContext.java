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

import java.util.List;

/**
 * The Class WebappContext.
 * 
 * @author cstamas
 */
public class WebappContext
{

    /** The name. */
    private String name;

    /** The context path. */
    private String contextPath;

    /** The authentication info. */
    private AuthenticationInfo authenticationInfo;

    /** The servlet infos. */
    private List<ServletInfo> servletInfos;

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name the new name
     */
    public void setName( String name )
    {
        this.name = name;
        setContextPath( "/" + name );
    }

    /**
     * Gets the context path.
     * 
     * @return the context path
     */
    public String getContextPath()
    {
        return contextPath;
    }

    /**
     * Sets the context path.
     * 
     * @param contextPath the new context path
     */
    public void setContextPath( String contextPath )
    {
        this.contextPath = contextPath;
    }

    /**
     * Gets the authentication info.
     * 
     * @return the authentication info
     */
    public AuthenticationInfo getAuthenticationInfo()
    {
        return authenticationInfo;
    }

    /**
     * Sets the authentication info.
     * 
     * @param authenticationInfo the new authentication info
     */
    public void setAuthenticationInfo( AuthenticationInfo authenticationInfo )
    {
        this.authenticationInfo = authenticationInfo;
    }

    /**
     * Gets the servlet infos.
     * 
     * @return the servlet infos
     */
    public List<ServletInfo> getServletInfos()
    {
        return servletInfos;
    }

    /**
     * Sets the servlet infos.
     * 
     * @param servletInfos the new servlet infos
     */
    public void setServletInfos( List<ServletInfo> servletInfos )
    {
        this.servletInfos = servletInfos;
    }

}
