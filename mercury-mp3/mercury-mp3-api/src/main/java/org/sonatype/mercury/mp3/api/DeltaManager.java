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

package org.sonatype.mercury.mp3.api;

import java.util.Collection;
import java.util.List;

import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.util.Monitor;
import org.sonatype.mercury.mp3.api.cd.ContainerConfig;
import org.sonatype.mercury.mp3.api.cd.NodeConfig;

/**
 * @author Oleg Gusakov
 * @version $Id$
 */
public interface DeltaManager
{
    public static final String ROLE = DeltaManager.class.getName();

    public static final String CD_EXT = "cd";

    public static final String LDL_EXT = "ldl";

    public static final String CD_DIR = "bin/.cd";

    /**
     * container type
     * 
     * @return
     */
    public String getContainerType();

    /**
     * up / down grade current configuration
     * 
     * @param configuration
     * @throws DeltaManagerException
     */
    public Collection<ContainerConfig> applyConfiguration( NodeConfig configuration, List<Repository> repos,
                                                           Monitor monitor )
        throws DeltaManagerException;
}
