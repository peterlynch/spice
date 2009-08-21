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

import java.util.List;
import javax.inject.Inject;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * JAVADOC
 *
 * @author Alin Dreghiciu
 */
public class InjectListOfUserCustomComponents
    implements ManagedInterface
{

    @Inject
    public List<UserCustomComponent> injectedViaInject;

    @Requirement ( role = UserCustomComponent.class )
    public List<UserCustomComponent> injectedViaRequirement;

    @Inject
    public List unknownRole;

    @Inject
    List<UserCustomComponent> packageInjectedViaInject;

    @Inject
    protected List<UserCustomComponent> protectedInjectedViaInject;

    public List<UserCustomComponent> getPackageInjectedViaInject()
    {
        return packageInjectedViaInject;
    }

    public List<UserCustomComponent> getProtectedInjectedViaInject()
    {
        return protectedInjectedViaInject;
    }

}
