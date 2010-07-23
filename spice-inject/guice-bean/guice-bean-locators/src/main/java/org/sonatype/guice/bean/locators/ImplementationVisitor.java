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

import com.google.inject.Binding;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.UntargettedBinding;

final class ImplementationVisitor
    extends DefaultBindingTargetVisitor<Object, Class<?>>
{
    @Override
    public Class<?> visit( final UntargettedBinding<?> binding )
    {
        return binding.getKey().getTypeLiteral().getRawType();
    }

    @Override
    public Class<?> visit( final LinkedKeyBinding<?> binding )
    {
        return binding.getLinkedKey().getTypeLiteral().getRawType();
    }

    @Override
    public Class<?> visit( final ConstructorBinding<?> binding )
    {
        return binding.getConstructor().getDeclaringType().getRawType();
    }

    @Override
    public Class<?> visit( final InstanceBinding<?> binding )
    {
        return binding.getInstance().getClass();
    }

    @Override
    protected Class<?> visitOther( final Binding<?> binding )
    {
        return Object.class;
    }
}
