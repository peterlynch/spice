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
