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
package org.sonatype.guice.bean.reflect;

import java.lang.annotation.Annotation;

import com.google.inject.TypeLiteral;

/**
 * Represents a bean property such as a field or setter method.
 * 
 * <pre>
 * &#064;SomeAnnotation
 * SomeType name;
 * 
 * &#064;SomeAnnotation
 * void setName( SomeType _name )
 * {
 *     //...
 * }
 * </pre>
 */
public interface BeanProperty<T>
{
    /**
     * Returns the property annotation with the specified type.
     * 
     * @param annotationType The annotation type
     * @return The property's annotation if it exists; otherwise {@code null}
     */
    <A extends Annotation> A getAnnotation( Class<A> annotationType );

    /**
     * @return The property's reified generic type
     */
    TypeLiteral<T> getType();

    /**
     * @return The property's name (excluding name-space)
     */
    String getName();

    /**
     * Set the property in the given component to the given value.
     * 
     * @param component The component
     * @param value The value to set
     */
    void set( Object component, T value );
}
