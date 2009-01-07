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
package org.sonatype.plexus.classworlds.validator;

import org.sonatype.plexus.classworlds.model.ClassworldsAppConfiguration;
import org.sonatype.plexus.classworlds.model.ClassworldsRealmConfiguration;

import java.util.Map;

public class ClassworldsModelValidator
{

    public ClassworldsValidationResult validate( ClassworldsAppConfiguration config )
    {
        ClassworldsValidationResult result = new ClassworldsValidationResult();
        if ( config.getMainClass() == null || config.getMainClass().trim().length() < 1 )
        {
            result.addError( "Main-Class is missing or invalid." );

            // normalize for further validation...
            config.setMainClass( null );
        }

        if ( config.getMainRealm() == null || config.getMainRealm().trim().length() < 1 )
        {
            result.addError( "Main-Realm is missing or invalid." );

            // normalize for further validation...
            config.setMainRealm( null );
        }

        Map<String, ClassworldsRealmConfiguration> realmConfigurations = config.getRealmConfigurationMap();
        if ( realmConfigurations == null || realmConfigurations.isEmpty() )
        {
            result.addError( "No realms configured." );
        }
        else if ( config.getMainRealm() != null && realmConfigurations.get( config.getMainRealm() ) == null )
        {
            result.addError( "Main-Realm: " + config.getMainRealm() + " is not configured." );
        }

        for ( Map.Entry<String, ClassworldsRealmConfiguration> entry : realmConfigurations.entrySet() )
        {
            Map<String, String> imports = entry.getValue().getImports();
            for ( Map.Entry<String, String> importEntry : imports.entrySet() )
            {
                if ( importEntry.getValue() == null )
                {
                    result.addError( "Realm: " + entry.getKey() + " specifies an import without a source realm (pattern: " + importEntry.getKey() );
                }
                else if ( realmConfigurations.get( importEntry.getValue() ) == null )
                {
                    result.addError( "Realm: " + entry.getKey() + " specifies an import from an unconfigured source realm: " + importEntry.getValue() );
                }
            }

            if ( entry.getValue().getParentRealmId() != null && realmConfigurations.get( entry.getValue().getParentRealmId() ) == null )
            {
                result.addError( "Realm: " + entry.getValue().getRealmId() + " refers to a parent realm that is not configured: " + entry.getValue().getParentRealmId() );
            }
        }

        return result;
    }

}
