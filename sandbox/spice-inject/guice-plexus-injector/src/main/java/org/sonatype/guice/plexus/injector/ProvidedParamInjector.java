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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.inject.Provider;
import com.google.inject.ProvisionException;

final class ProvidedParamInjector
    implements PropertyInjector
{
    final Method method;

    final Provider<?> provider;

    ProvidedParamInjector( final Method method, final Provider<?> provider )
    {
        this.method = method;
        this.provider = provider;
    }

    public void inject( final Object component )
    {
        try
        {
            method.invoke( component, provider.get() );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ProvisionException( e.toString() );
        }
        catch ( final InvocationTargetException e )
        {
            throw new ProvisionException( e.toString() );
        }
    }
}