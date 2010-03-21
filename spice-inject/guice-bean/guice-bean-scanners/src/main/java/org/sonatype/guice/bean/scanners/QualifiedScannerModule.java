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
package org.sonatype.guice.bean.scanners;

import java.net.URL;
import java.util.Enumeration;

import org.sonatype.guice.bean.reflect.ClassSpace;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Guice {@link Module} that discovers and auto-wires qualified beans contained in a {@link ClassSpace}.
 */
public final class QualifiedScannerModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public QualifiedScannerModule( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        final QualifiedClassVisitor visitor = new QualifiedClassVisitor( space, binder );
        try
        {
            final Enumeration<URL> e = space.findEntries( null, "*.class", true );
            while ( e.hasMoreElements() )
            {
                try
                {
                    // process potential bean class
                    visitor.scan( e.nextElement() );
                }
                catch ( final Throwable t )
                {
                    binder.addError( t );
                }
            }
        }
        catch ( final Throwable t )
        {
            binder.addError( t );
        }
    }
}
