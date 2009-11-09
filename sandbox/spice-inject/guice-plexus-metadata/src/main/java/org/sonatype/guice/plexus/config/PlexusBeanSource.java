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
package org.sonatype.guice.plexus.config;

/**
 * Discovers Plexus bean implementations along with any associated metadata.
 */
public interface PlexusBeanSource
{
    /**
     * Finds Plexus beans by scanning, configuration, or other similar methods.
     * 
     * @return Sequence of bean implementations
     */
    Iterable<Class<?>> findPlexusBeans();

    /**
     * Returns metadata associated with the given Plexus bean implementation.
     * 
     * @param implementation The bean implementation
     * @return Metadata associated with the given bean
     */
    PlexusBeanMetadata getBeanMetadata( Class<?> implementation );
}