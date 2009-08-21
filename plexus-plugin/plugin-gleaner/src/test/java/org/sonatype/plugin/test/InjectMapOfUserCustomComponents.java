/*
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
 *
 */
package org.sonatype.plugin.test;

import java.util.Map;
import javax.inject.Inject;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * JAVADOC
 *
 * @author Alin Dreghiciu
 */
public class InjectMapOfUserCustomComponents
    implements ManagedInterface
{

    @Inject
    public Map<String, UserCustomComponent> injectedViaInject;

    @Requirement ( role = UserCustomComponent.class )
    public Map<String, UserCustomComponent> injectedViaRequirement;

    @Inject
    public Map unknownRole;

    @Inject
    Map<String, UserCustomComponent> packageInjectedViaInject;

    @Inject
    protected Map<String, UserCustomComponent> protectedInjectedViaInject;

    public Map<String, UserCustomComponent> getPackageInjectedViaInject()
    {
        return packageInjectedViaInject;
    }

    public Map<String, UserCustomComponent> getProtectedInjectedViaInject()
    {
        return protectedInjectedViaInject;
    }

}