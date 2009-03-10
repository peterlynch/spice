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
    
    /** The servlets name */
    private String name;

    /** Set the initialize order.
     * Holders with order<0, are initialized on use. Those with
     * order>=0 are initialized in increasing order when the handler
     * is started.
     */
    private int initOrder;
    
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

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public int getInitOrder()
    {
        return this.initOrder;
    }
    
    public void setInitOrder( int initOrder )
    {
        this.initOrder = initOrder;
    }

}
