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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Represents an abstract collection of related classes and resources.
 */
public interface ClassSpace
{
    /**
     * Load the named class from the surrounding class space.
     * 
     * @param name The class name
     * @return Class instance
     * @see ClassLoader#loadClass(String)
     */
    Class<?> loadClass( String name )
        throws ClassNotFoundException;

    /**
     * Defer loading the named class from the surrounding class space.
     * 
     * @param name The class name
     * @return Deferred class
     * @see ClassLoader#loadClass(String)
     */
    DeferredClass<?> deferLoadClass( String name );

    /**
     * Queries the class space for resources matching the given name.
     * 
     * @param name The resource name
     * @return Series of URLs
     * @see ClassLoader#getResources(String)
     */
    Enumeration<URL> getResources( String name )
        throws IOException;
}
