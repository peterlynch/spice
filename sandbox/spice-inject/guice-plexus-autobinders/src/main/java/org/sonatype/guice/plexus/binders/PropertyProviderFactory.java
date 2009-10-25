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

import java.lang.annotation.Annotation;

import org.sonatype.guice.bean.reflect.BeanProperty;

import com.google.inject.Provider;

/**
 * Factory that supplies {@link Provider}s for properties with particular annotations.
 */
interface PropertyProviderFactory<A extends Annotation>
{
    /**
     * Returns a {@link Provider} based on the given annotation for the given property.
     * 
     * @param annotation The property annotation
     * @param property The bean property
     * @return Provider that can provide values for the given property
     */
    <T> Provider<T> lookup( A annotation, BeanProperty<T> property );
}
