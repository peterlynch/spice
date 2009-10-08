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
package org.jsecurity.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * MethodInterceptor that inspects a specific annotation on the method invocation before continuing
 * its execution.
 *
 * @author Les Hazlewood
 * @since 0.9
 */
public abstract class AnnotationMethodInterceptor extends MethodInterceptorSupport {

    protected Class<? extends Annotation> annotationClass;

    public AnnotationMethodInterceptor( Class<? extends Annotation> annotationClass ) {
        setAnnotationClass(annotationClass);
    }

    protected void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        if (annotationClass == null) {
            String msg = "annotationClass argument cannot be null";
            throw new IllegalArgumentException(msg);
        }
        this.annotationClass = annotationClass;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return this.annotationClass;
    }

    public boolean supports(MethodInvocation mi) {
        return getAnnotation(mi) != null;
    }

    protected Annotation getAnnotation(MethodInvocation mi) {
        if (mi == null) {
            throw new IllegalArgumentException("method argument cannot be null");
        }
        Method m = mi.getMethod();
        if (m == null) {
            String msg = MethodInvocation.class.getName() + " parameter incorrectly " +
                    "constructed.  getMethod() returned null";
            throw new IllegalArgumentException(msg);

        }
        return m.getAnnotation(getAnnotationClass());

    }

}
