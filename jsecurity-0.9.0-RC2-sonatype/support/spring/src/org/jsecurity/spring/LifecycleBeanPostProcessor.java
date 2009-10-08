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
package org.jsecurity.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.util.Destroyable;
import org.jsecurity.util.Initializable;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * <p>Bean post processor for Spring that automatically calls the <tt>init()</tt> and/or
 * <tt>destroy()</tt> methods on JSecurity objects that implement the {@link org.jsecurity.util.Initializable}
 * or {@link org.jsecurity.util.Destroyable} interfaces, respectfully.  This post processor makes it easier
 * to configure JSecurity beans in Spring, since the user never has to worry about whether or not if they
 * have to specify init-method and destroy-method bean attributes.</p>
 *
 * <p><b>Warning: This post processor has no way to determine if <tt>init()</tt> or <tt>destroy()</tt> have
 * already been called, so if you define this post processor in your applicationContext, do not also call these
 * methods manually or via Spring's <tt>init-method</tt> or <tt>destroy-method</tt> bean attributes.</b></p>
 *
 * @author Jeremy Haile
 * @since 0.2
 */
public class LifecycleBeanPostProcessor implements DestructionAwareBeanPostProcessor {

    private static final Log log = LogFactory.getLog(LifecycleBeanPostProcessor.class);

    /**
     * Calls the <tt>init()</tt> methods on the bean if it implements {@link org.jsecurity.util.Initializable}
     *
     * @param object the object being initialized.
     * @param name   the name of the bean being initialized.
     * @return the initialized bean.
     * @throws BeansException if any exception is thrown during initialization.
     */
    public Object postProcessBeforeInitialization(Object object, String name) throws BeansException {
        if (object instanceof Initializable) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing bean [" + name + "]...");
                }

                ((Initializable) object).init();
            } catch (Exception e) {
                throw new FatalBeanException("Error initializing bean [" + name + "]", e);
            }
        }
        return object;
    }


    public Object postProcessAfterInitialization(Object object, String name) throws BeansException {
        // Does nothing after initialization
        return object;
    }


    /**
     * Calls the <tt>destroy()</tt> methods on the bean if it implements {@link org.jsecurity.util.Destroyable}
     *
     * @param object the object being initialized.
     * @param name   the name of the bean being initialized.
     * @throws BeansException if any exception is thrown during initialization.
     */
    public void postProcessBeforeDestruction(Object object, String name) throws BeansException {
        if (object instanceof Destroyable) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Destroying bean [" + name + "]...");
                }

                ((Destroyable) object).destroy();
            } catch (Exception e) {
                throw new FatalBeanException("Error destroying bean [" + name + "]", e);
            }
        }
    }
}