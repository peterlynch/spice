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
package org.sonatype.guice.plexus.annotations;

import java.lang.annotation.Annotation;

import org.codehaus.plexus.component.annotations.Component;

/**
 * Partial runtime implementation of Plexus {@link Component} annotation, supporting the most common properties.
 */
public final class ComponentImpl
    implements Component
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    // pre-computed hashCode representing fixed properties
    private static final int HASH_CODE_OFFSET = 71812305;

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<?> role;

    private final String hint;

    private final String version;

    private final String instantiationStrategy;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ComponentImpl( final Class<?> role, final String hint, final String version,
                          final String instantiationStrategy )
    {
        if ( null == role || null == hint || null == version || null == instantiationStrategy )
        {
            throw new IllegalArgumentException( "@Component cannot contain null values" );
        }

        this.role = role;
        this.hint = hint;
        this.version = version;
        this.instantiationStrategy = instantiationStrategy;
    }

    // ----------------------------------------------------------------------
    // Annotation properties
    // ----------------------------------------------------------------------

    public Class<?> role()
    {
        return role;
    }

    public String hint()
    {
        return hint;
    }

    public String version()
    {
        return version;
    }

    public String instantiationStrategy()
    {
        return instantiationStrategy;
    }

    public boolean isolatedRealm()
    {
        return false;
    }

    public String alias()
    {
        return "";
    }

    public String composer()
    {
        return "";
    }

    public String configurator()
    {
        return "";
    }

    public String description()
    {
        return "";
    }

    public String factory()
    {
        return "";
    }

    public String lifecycleHandler()
    {
        return "";
    }

    public String profile()
    {
        return "";
    }

    public String type()
    {
        return "";
    }

    // ----------------------------------------------------------------------
    // Standard annotation behaviour
    // ----------------------------------------------------------------------

    @Override
    public boolean equals( final Object rhs )
    {
        if ( rhs instanceof Component )
        {
            final Component cmp = (Component) rhs;

            return role.equals( cmp.role() ) && hint.equals( cmp.hint() ) && version.equals( cmp.version() )
                && instantiationStrategy.equals( cmp.instantiationStrategy() );
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return HASH_CODE_OFFSET + ( 127 * "role".hashCode() ^ role.hashCode() )
            + ( 127 * "hint".hashCode() ^ hint.hashCode() ) + ( 127 * "version".hashCode() ^ version.hashCode() )
            + ( 127 * "instantiationStrategy".hashCode() ^ instantiationStrategy.hashCode() );
    }

    @Override
    public String toString()
    {
        return String.format( "@%s(isolatedRealm=false, composer=, configurator=, alias=, description=, "
            + "instantiationStrategy=%s, factory=, hint=%s, type=, lifecycleHandler=, version=%s, "
            + "profile=, role=%s)", Component.class.getName(), instantiationStrategy, hint, version, role );
    }

    public Class<? extends Annotation> annotationType()
    {
        return Component.class;
    }
}