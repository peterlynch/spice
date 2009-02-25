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


package org.sonatype.mercury.configurator;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.util.Monitor;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface MercuryConfigurator
{
    public static final String USER_HOME = System.getProperty( "user.home" );

    public static final String DEFAULT_SETTINGS = USER_HOME + "/.m2/settings.xml";

    public static final String SYSTEM_PROPERTY_MONITOR = "mercury.monitor";

    public static final String SYSTEM_PROPERTY_LOCAL_REPO = "maven.repo.local";

    public static final String SYSTEM_PROPERTY_REMOTE_REPO = "maven.repo.remote";

    public static final char OPTION_SETTINGS = 's';

    public static final char OPTION_SHOW_DETAILS = 'd';

    public static final char OPTION_OFFLINE = 'o';
    
    public static final String SYSTEM_PROPERTY_DEFAULT_LOCAL_REPO = "maven.repo.local";
    public static final String DEFAULT_LOCAL_REPO = System.getProperty(   
                                                   SYSTEM_PROPERTY_DEFAULT_LOCAL_REPO
                                                 , USER_HOME+"/.m2/repository"
                                                                       );
    
    public static final String SYSTEM_PROPERTY_DEFAULT_CENTRAL = "maven.repo.central";
    public static final String DEFAULT_CENTRAL = System.getProperty( SYSTEM_PROPERTY_DEFAULT_CENTRAL
//                                                                     , "http://repo1.maven.org/maven2"
                                                                     , "http://repository.sonatype.org/content/groups/public/"
                                                                   );
    public Monitor getMonitor( CommandLine cli )
    throws MercuryConfiguratorException
    ;

    public List<Repository> getRepositories( CommandLine cli )
    throws MercuryConfiguratorException
    ;
}
