/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.locators;

import java.lang.annotation.Annotation;

import javax.inject.Qualifier;

import org.sonatype.inject.Mediator;

import com.google.inject.ImplementedBy;
import com.google.inject.Key;

/**
 * Dynamic locator of beans annotated with {@link Qualifier} annotations.
 */
@ImplementedBy( MutableBeanLocator.class )
public interface BeanLocator
{
    /**
     * Locates beans that match the given qualified binding {@link Key}.
     * 
     * @param key The qualified key
     * @param notify Optional callback; called when the bean sequence changes
     * @return Sequence of beans that match the given key
     */
    <Q extends Annotation, T> Iterable<QualifiedBean<Q, T>> locate( Key<T> key, Runnable notify );

    /**
     * Watches out for beans that match the given qualified binding {@link Key}. <br>
     * The {@link Mediator} mediates bean events between locator and watcher.
     * 
     * @param key The qualified key
     * @param mediator The update mediator
     * @param watcher The bean watcher
     */
    <Q extends Annotation, T, W> void watch( Key<T> key, Mediator<Q, T, W> mediator, W watcher );
}
