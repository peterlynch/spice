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

import java.util.Properties;

/**
 * The Class ServletInfo.
 * 
 * @author cstamas
 */
public class ServletInfo
{
    
    /** The mapping. */
    private String mapping;
    
    /** The servlet class. */
    private String servletClass;

    /** The parameters. */
    private Properties parameters;
    
    /**
     * Gets the parameters.
     * 
     * @return the parameters
     */
    public Properties getParameters()
    {
        return parameters;
    }

    /**
     * Sets the parameters.
     * 
     * @param parameters the new parameters
     */
    public void setParameters( Properties parameters )
    {
        this.parameters = parameters;
    }

    /**
     * Gets the mapping.
     * 
     * @return the mapping
     */
    public String getMapping()
    {
        return mapping;
    }

    /**
     * Sets the mapping.
     * 
     * @param mapping the new mapping
     */
    public void setMapping( String mapping )
    {
        this.mapping = mapping;
    }

    /**
     * Gets the servlet class.
     * 
     * @return the servlet class
     */
    public String getServletClass()
    {
        return servletClass;
    }

    /**
     * Sets the servlet class.
     * 
     * @param servletClass the new servlet class
     */
    public void setServletClass( String servletClass )
    {
        this.servletClass = servletClass;
    }

}
