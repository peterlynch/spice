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

import org.codehaus.plexus.component.annotations.Configuration;

public final class ConfigurationImpl
    implements Configuration
{
    private final String name;

    private final String value;

    public ConfigurationImpl( final String name, final String value )
    {
        if ( null == name || null == value )
        {
            throw new IllegalArgumentException( "@" + Configuration.class.getName() + " cannot contain null values" );
        }

        this.name = name;
        this.value = value;
    }

    public String name()
    {
        return name;
    }

    public String value()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return ( 127 * "name".hashCode() ^ name.hashCode() ) + ( 127 * "value".hashCode() ^ value.hashCode() );
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( rhs instanceof Configuration )
        {
            final Configuration conf = (Configuration) rhs;

            return name.equals( conf.name() ) && value.equals( conf.value() );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "@" + Configuration.class.getName() + "(name=" + name + ", value=" + value + ")";
    }

    public Class<? extends Annotation> annotationType()
    {
        return Configuration.class;
    }
}