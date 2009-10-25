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
package org.sonatype.guice.plexus.binders;

import org.sonatype.guice.bean.injector.BeanPropertyBinding;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

/**
 * Represents a property element (such as a field or setter method) that can be injected.
 */
interface InjectableProperty<T>
{
    /**
     * @return The property's reified generic type
     */
    TypeLiteral<T> getType();

    /**
     * @return The property's name (excluding name-space)
     */
    String getName();

    /**
     * Creates a {@link BeanPropertyBinding} between the current element and the given provider.
     * 
     * @param provider A provider of values for the property
     * @return Property binding that uses the given provider
     */
    BeanPropertyBinding bind( Provider<T> provider );
}
