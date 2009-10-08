/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jsecurity.authz.aop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.aop.MethodInvocation;
import org.jsecurity.authz.AuthorizationException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An <tt>AnnotationsAuthorizingMethodInterceptor</tt> is a MethodInterceptor that asserts a given method is authorized
 * to execute based on one or more configured <tt>AuthorizingAnnotationMethodInterceptor</tt>s.
 *
 * <p>This allows multiple annotations on a method to be processed before the method
 * executes, and if any of the <tt>AuthorizingAnnotationMethodInterceptor</tt>s indicate that the method should not be
 * executed, an <tt>AuthorizationException</tt> will be thrown, otherwise the method will be invoked as expected.
 *
 * <p>It is essentially a convenience mechanism to allow multiple annotations to be processed in a single method
 * interceptor.
 *
 * @author Les Hazlewood
 * @since 0.2
 */
public abstract class AnnotationsAuthorizingMethodInterceptor extends AuthorizingMethodInterceptor {

    private static final Log log = LogFactory.getLog(AnnotationsAuthorizingMethodInterceptor.class);    

    protected Collection<AuthorizingAnnotationMethodInterceptor> methodInterceptors;

    public AnnotationsAuthorizingMethodInterceptor() {
        methodInterceptors = new ArrayList<AuthorizingAnnotationMethodInterceptor>(2);
        methodInterceptors.add(new RoleAnnotationMethodInterceptor());
        methodInterceptors.add(new PermissionAnnotationMethodInterceptor());
    }

    public Collection<AuthorizingAnnotationMethodInterceptor> getMethodInterceptors() {
        return methodInterceptors;
    }

    public void setMethodInterceptors(Collection<AuthorizingAnnotationMethodInterceptor> methodInterceptors) {
        this.methodInterceptors = methodInterceptors;
    }

    protected void assertAuthorized(MethodInvocation methodInvocation) throws AuthorizationException {
        //default implementation just ensures no deny votes are cast:
        Collection<AuthorizingAnnotationMethodInterceptor> aamis = getMethodInterceptors();
        if (aamis != null && !aamis.isEmpty()) {
            for (AuthorizingAnnotationMethodInterceptor aami : aamis) {
                if (aami.supports(methodInvocation)) {
                    aami.assertAuthorized(methodInvocation);
                }
            }
        }
    }
}
