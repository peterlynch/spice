/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.sonatype.mercury.mp3.delta.cli;

import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactQueryList;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.plexus.PlexusMercury;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.util.Monitor;
import org.apache.maven.mercury.util.Util;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class DependencyUtil
{
    PlexusMercury _mercury;

    /**
     * 
     */
    public DependencyUtil( PlexusMercury mercury )
    {
        _mercury = mercury;
    }

    public List<ArtifactMetadata> floatSnapshot( ArtifactQueryList deps
                                     , String nameRe
                                     , String sn
                                     , String ts
                                     , ArtifactScopeEnum scope
                                     , List<Repository> repos
                                     , Monitor monitor
                                  )
    throws RepositoryException
    {
        ArtifactQueryList list = new ArtifactQueryList( deps.getMetadataList() );
        List<ArtifactMetadata> res = null;
        
        for( boolean dirty = true ; dirty ;)
        {
            res = _mercury.resolve( repos, scope, list, null, null );
            
            if( Util.isEmpty( res ) )
                return null;
            
            dirty = false;
            
            for( ArtifactMetadata md : res )
            {
                if( md.getArtifactId().matches( nameRe ) && sn.equals( md.getVersion() ) )
                {
                    md.setVersion( ts );
                    list.add( md );
                    dirty = true;
                }
            }
        }
        
        return res;
    }
}
