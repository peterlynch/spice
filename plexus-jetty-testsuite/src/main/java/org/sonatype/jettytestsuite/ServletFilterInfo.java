package org.sonatype.jettytestsuite;

import java.util.Properties;

public class ServletFilterInfo
{

    /** The mapping. */
    private String mapping;
    
    /** The filter class. */
    private String filterClass;

    /** The parameters. */
    private Properties parameters;

    public String getMapping()
    {
        return mapping;
    }

    public void setMapping( String mapping )
    {
        this.mapping = mapping;
    }

    public String getFilterClass()
    {
        return filterClass;
    }

    public void setFilterClass( String filterClass )
    {
        this.filterClass = filterClass;
    }

    public Properties getParameters()
    {
        return parameters;
    }

    public void setParameters( Properties parameters )
    {
        this.parameters = parameters;
    }
}
