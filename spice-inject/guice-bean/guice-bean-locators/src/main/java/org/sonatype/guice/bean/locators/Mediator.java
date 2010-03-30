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
import java.util.Map.Entry;

/**
 * Mediates updates between the {@link BeanLocator} and a bean watcher.
 */
public interface Mediator<Q extends Annotation, T, W>
{
    /**
     * Inform the given watcher about the added bean.
     * 
     * @param bean The added bean
     * @param watcher The bean watcher
     */
    void add( Entry<Q, T> bean, W watcher )
        throws Exception;

    /**
     * Inform the given watcher about the removed bean.
     * 
     * @param bean The removed bean
     * @param watcher The bean watcher
     */
    void remove( Entry<Q, T> bean, W watcher )
        throws Exception;
}
