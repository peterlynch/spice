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
package org.sonatype.guice.bean.inject;

import org.sonatype.guice.bean.reflect.BeanProperty;

/**
 * Auto-binds bean properties such as fields or setter methods.
 */
public interface BeanPropertyBinder
{
    /**
     * Returns the appropriate {@link BeanPropertyBinding} for the given bean property.
     * 
     * @param property The property
     * @return Binding for the given property; {@code null} if no binding is applicable
     */
    <T> BeanPropertyBinding bindProperty( BeanProperty<T> property );

    /**
     * Special binding that indicates the binder is not able to provide any more bindings.
     */
    BeanPropertyBinding LAST_BINDING = new BeanPropertyBinding()
    {
        public void injectProperty( final Object bean )
        {
            // nothing to do, for marking purposes only
        }
    };
}
