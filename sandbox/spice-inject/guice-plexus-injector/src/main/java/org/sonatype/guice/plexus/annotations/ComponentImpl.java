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

public final class ComponentImpl
    implements Component
{
    private static final int HASH_CODE_OFFSET = 1776358393;

    private final Class<?> role;

    private final String hint;

    private final String instantiationStrategy;

    public ComponentImpl( final Class<?> role, final String hint, final String instantiationStrategy )
    {
        if ( null == role || null == hint || null == instantiationStrategy )
        {
            throw new IllegalArgumentException( "@" + Component.class.getName() + " cannot contain null values" );
        }

        this.role = role;
        this.hint = hint;

        this.instantiationStrategy = instantiationStrategy;
    }

    public Class<?> role()
    {
        return role;
    }

    public String hint()
    {
        return hint;
    }

    public String instantiationStrategy()
    {
        return instantiationStrategy;
    }

    @Override
    public int hashCode()
    {
        return ( 127 * "role".hashCode() ^ role.hashCode() ) + ( 127 * "hint".hashCode() ^ hint.hashCode() )
            + ( 127 * "instantiationStrategy".hashCode() ^ instantiationStrategy.hashCode() ) + HASH_CODE_OFFSET;
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( rhs instanceof Component )
        {
            final Component req = (Component) rhs;

            return role.equals( req.role() ) && hint.equals( req.hint() )
                && instantiationStrategy.equals( req.instantiationStrategy() );
        }

        return false;
    }

    @Override
    public String toString()
    {
        return "@" + Component.class.getName() + "(role=" + role + ", hint=" + hint + ", instantiationStrategy="
            + instantiationStrategy + ")";
    }

    public Class<? extends Annotation> annotationType()
    {
        return Component.class;
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

    public boolean isolatedRealm()
    {
        return false;
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

    public String version()
    {
        return "";
    }
}