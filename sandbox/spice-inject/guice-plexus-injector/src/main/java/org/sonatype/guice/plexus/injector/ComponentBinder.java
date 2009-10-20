/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.plexus.injector;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * Auto-binds components that contain one or more property elements.
 */
public interface ComponentBinder
{
    /**
     * Returns the appropriate {@link PropertyBinder} for the given component type.
     * 
     * @param encounter The Guice type encounter
     * @param type The component type
     * @return Property binder for the given type; {@code null} if no binder is applicable
     */
    <T> PropertyBinder bindComponent( TypeEncounter<T> encounter, TypeLiteral<T> type );
}
