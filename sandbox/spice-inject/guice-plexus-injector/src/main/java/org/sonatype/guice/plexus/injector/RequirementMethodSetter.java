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

import java.lang.reflect.Method;

import org.codehaus.plexus.component.annotations.Requirement;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * {@link PropertyInjector} that injects a component into a single-argument method marked with {@link Requirement}.
 */
final class RequirementMethodSetter
    extends AbstractRequirementSetter
{
    final Method method;

    final Provider<?> delegate;

    /**
     * Wire up the appropriate component {@link Provider} but don't call it yet.
     * 
     * @param encounter our link back to Guice
     * @param method a single-argument method
     */
    RequirementMethodSetter( final TypeEncounter<?> encounter, final Method method )
    {
        super( encounter );
        this.method = method;

        final TypeLiteral<?> targetType = TypeLiteral.get( method.getGenericParameterTypes()[0] );
        delegate = lookup( targetType, method.getAnnotation( Requirement.class ) );
    }

    @Override
    protected void privilegedApply( final Object instance )
        throws Exception
    {
        method.setAccessible( true ); // FindBugs DP_DO_INSIDE_DO_PRIVILEGED false-positive
        method.invoke( instance, delegate.get() );
    }
}
