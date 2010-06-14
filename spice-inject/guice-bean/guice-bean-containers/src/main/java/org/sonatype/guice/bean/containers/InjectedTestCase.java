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
package org.sonatype.guice.bean.containers;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

/**
 * Abstract {@link TestCase} that automatically binds and injects itself.
 */
public abstract class InjectedTestCase
    extends TestCase
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String basedir;

    @Inject
    private BeanLocator locator;

    // ----------------------------------------------------------------------
    // Setup
    // ----------------------------------------------------------------------

    @Override
    protected void setUp()
        throws Exception
    {
        final ClassSpace space = new URLClassSpace( (URLClassLoader) getClass().getClassLoader() );
        Guice.createInjector( new WireModule( new TestModule(), new SpaceModule( space ) ) );
    }

    final class TestModule
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install( InjectedTestCase.this );

            final Map<String, Object> properties = new HashMap<String, Object>();
            properties.put( "basedir", getBasedir() );
            InjectedTestCase.this.configure( properties );
            bind( WireModule.CONFIG_KEY ).toInstance( properties );

            requestInjection( InjectedTestCase.this );
        }
    }

    // ----------------------------------------------------------------------
    // Container configuration methods
    // ----------------------------------------------------------------------

    /**
     * Custom injection bindings.
     */
    public void configure( final Binder binder )
    {
        // place any per-test bindings here...
    }

    /**
     * Custom property values.
     */
    public void configure( final Map<String, Object> properties )
    {
        // put any per-test properties here...
    }

    // ----------------------------------------------------------------------
    // Container lookup methods
    // ----------------------------------------------------------------------

    public final <T> T lookup( final Class<T> type )
    {
        return lookup( Key.get( type ) );
    }

    public final <T> T lookup( final Class<T> type, final String name )
    {
        return lookup( Key.get( type, Names.named( name ) ) );
    }

    public final <T> T lookup( final Key<T> key )
    {
        final Iterator<Entry<Annotation, T>> i = locator.locate( key ).iterator();
        return i.hasNext() ? i.next().getValue() : null;
    }

    // ----------------------------------------------------------------------
    // Container resource methods
    // ----------------------------------------------------------------------

    public final String getBasedir()
    {
        if ( null == basedir )
        {
            basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );
        }
        return basedir;
    }
}
