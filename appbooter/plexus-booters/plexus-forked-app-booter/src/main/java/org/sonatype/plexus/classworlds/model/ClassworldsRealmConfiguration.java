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
package org.sonatype.plexus.classworlds.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @todo 'optionally' keyword support.
 */
public class ClassworldsRealmConfiguration
{

    private Map<String, String> imports = new HashMap<String, String>();

    private LinkedHashSet<String> loadPatterns = new LinkedHashSet<String>();

    private final String realmId;

    private String parentRealmId;

    public ClassworldsRealmConfiguration( String realmId )
    {
        this.realmId = realmId;
    }

    public String getRealmId()
    {
        return realmId;
    }

    public String getId()
    {
        return ( parentRealmId == null ? "" : parentRealmId + "." ) + realmId;
    }

    public Map<String, String> getImports()
    {
        return imports;
    }

    public ClassworldsRealmConfiguration setImports( Map<String, String> imports )
    {
        this.imports = imports;

        return this;
    }

    public ClassworldsRealmConfiguration addImport( String pattern, String fromRealm )
    {
        imports.put( pattern, fromRealm );

        return this;
    }

    public boolean hasImports()
    {
        return !imports.isEmpty();
    }

    public LinkedHashSet<String> getLoadPatterns()
    {
        return loadPatterns;
    }

    public ClassworldsRealmConfiguration setLoadPatterns( LinkedHashSet<String> loadPatterns )
    {
        this.loadPatterns = loadPatterns;

        return this;
    }

    public ClassworldsRealmConfiguration addLoadPattern( String loadPattern )
    {
        loadPatterns.add( loadPattern );

        return this;
    }

    public ClassworldsRealmConfiguration addLoadPatterns( Collection<String> dependencyPaths )
    {
        loadPatterns.addAll( dependencyPaths );

        return this;
    }

    public boolean hasLoadPatterns()
    {
        return !loadPatterns.isEmpty();
    }

    public ClassworldsRealmConfiguration setParentRealm( String parentRealmId )
    {
        this.parentRealmId = parentRealmId;

        return this;
    }

    public String getParentRealmId()
    {
        return parentRealmId;
    }

}
