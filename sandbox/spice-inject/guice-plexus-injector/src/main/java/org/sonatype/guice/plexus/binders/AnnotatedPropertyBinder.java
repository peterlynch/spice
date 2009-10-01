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

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

/**
 * Binds annotated properties to {@link Provider}s.
 */
public interface AnnotatedPropertyBinder<A extends Annotation>
{
    /**
     * Returns a {@link Provider} for the given annotated property.
     * 
     * <pre>
     * &#064;A
     * T name; // example property field
     * 
     * &#064;A
     * void setName(T); // example property setter
     * </pre>
     * 
     * @param annotation the annotation on the property
     * @param type the (generic) type of the property
     * @param fqn the fully qualified name of the property
     * @return a provider of values for the property
     */
    Provider<?> bindProperty( A annotation, TypeLiteral<?> type, String fqn );
}
