package org.sonatype.ldaptestsuite;

import java.io.File;
import java.util.List;

public class Partition
{
    
    private String name;
    
    private String suffix;
    
    private List<String> indexedAttributes;
    
    private List<String> rootEntryClasses;
    
    private File ldifFile;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getSuffix()
    {
        return suffix;
    }

    public void setSuffix( String suffix )
    {
        this.suffix = suffix;
    }

    public List<String> getIndexedAttributes()
    {
        return indexedAttributes;
    }

    public void setIndexedAttributes( List<String> indexedAttributes )
    {
        this.indexedAttributes = indexedAttributes;
    }

    public List<String> getRootEntryClasses()
    {
        return rootEntryClasses;
    }

    public void setRootEntryClasses( List<String> rootClasses )
    {
        this.rootEntryClasses = rootClasses;
    }

    public File getLdifFile()
    {
        return ldifFile;
    }

    public void setLdifFile( File ldifFile )
    {
        this.ldifFile = ldifFile;
    }

}
