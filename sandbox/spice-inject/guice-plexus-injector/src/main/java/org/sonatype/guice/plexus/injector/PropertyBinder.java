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

import java.lang.reflect.AnnotatedElement;

/**
 * Auto-binds property elements such as fields or setter methods.
 */
public interface PropertyBinder
{
    /**
     * Returns the appropriate {@link PropertyBinding} for the given property element.
     * 
     * @param element The property element
     * @return Property binding for the given element; {@code null} if no binding is applicable
     */
    PropertyBinding bindProperty( AnnotatedElement element );

    /**
     * Indicates the property binder has no more bindings for the current component type.
     */
    PropertyBinding LAST_BINDING = new PropertyBinding()
    {
        public void injectProperty( final Object component )
        {
            // nothing to do, for marking purposes only
        }
    };
}
