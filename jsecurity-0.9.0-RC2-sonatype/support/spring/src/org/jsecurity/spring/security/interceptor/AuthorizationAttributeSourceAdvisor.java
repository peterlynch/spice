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
package org.jsecurity.spring.security.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.authz.annotation.RequiresPermissions;
import org.jsecurity.authz.annotation.RequiresRoles;
import org.jsecurity.mgt.SecurityManager;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;

/**
 * @author Les Hazlewood
 * @since 0.1
 */
public class AuthorizationAttributeSourceAdvisor extends StaticMethodMatcherPointcutAdvisor
        implements InitializingBean {

    private static final Log log = LogFactory.getLog(AuthorizationAttributeSourceAdvisor.class);

    protected SecurityManager securityManager = null;

    /**
     * Create a new AuthorizationAttributeSourceAdvisor.
     */
    public AuthorizationAttributeSourceAdvisor() {
    }

    public org.jsecurity.mgt.SecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /**
     * Returns <tt>true</tt> if the method has a JSecurity <tt>RequiresRoles</tt> or
     * <tt>RequiresPermissions</tt> annotation, false otherwise.
     *
     * @param method      the method to check for a JSecurity annotation
     * @param targetClass the class potentially declaring JSecurity annotations
     * @return <tt>true</tt> if the method has a JSecurity <tt>RequiresRoles</tt> or
     *         <tt>RequiresPermissions</tt> annotation, false otherwise.
     * @see org.jsecurity.authz.annotation.RequiresRoles
     * @see org.jsecurity.authz.annotation.RequiresPermissions
     * @see org.springframework.aop.MethodMatcher#matches(java.lang.reflect.Method, Class)
     */
    public boolean matches(Method method, Class targetClass) {
        return ((method.getAnnotation(RequiresPermissions.class) != null) ||
                (method.getAnnotation(RequiresRoles.class) != null));
    }

    public void afterPropertiesSet() throws Exception {
        if (getAdvice() == null) {
            if (log.isTraceEnabled()) {
                log.trace("No authorization advice explicitly configured via the 'advice' " +
                        "property.  Attempting to set " +
                        "default instance of type [" +
                        AopAllianceAnnotationsAuthorizingMethodInterceptor.class.getName() + "]");
            }
            AopAllianceAnnotationsAuthorizingMethodInterceptor interceptor =
                    new AopAllianceAnnotationsAuthorizingMethodInterceptor();
            setAdvice(interceptor);
        }
    }
}
