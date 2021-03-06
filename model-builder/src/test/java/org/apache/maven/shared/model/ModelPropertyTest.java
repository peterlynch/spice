/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.apache.maven.shared.model;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ModelPropertyTest
{

    @Test
    public void isParent()
    {
        ModelProperty mp0 = new ModelProperty( "http://apache.org/maven/project/profiles#collection/profile/id", "1" );
        ModelProperty mp1 = new ModelProperty(
            "http://apache.org/maven/project/profiles#collection/profile/build/plugins/plugin/groupId", "org" );
        assertFalse( mp0.isParentOf( mp1 ) );
        assertTrue( mp0.getDepth() < mp1.getDepth() );
    }

    @Test
    public void isParent1()
    {
        ModelProperty mp0 = new ModelProperty( "http://apache.org/maven/project/profiles#collection/profile/id", "1" );
        ModelProperty mp1 =
            new ModelProperty( "http://apache.org/maven/project/profiles#collection/profile/id/a/b", "org" );
        assertFalse( mp0.isParentOf( mp1 ) );
    }
    @Test
    public void isParentExcludingProperty()
    {
        ModelProperty mp0 = new ModelProperty( "http://apache.org/maven/project/copy/fileset#property/dir", "target" );
        ModelProperty mp1 =
            new ModelProperty( "http://apache.org/maven/project/copy#property/todir", "src" );
        assertTrue( mp1.isParentOfExcludingProperties(mp0));
    } 
    
    @Test
    public void isParentExcludingPropertyParentNoProperty()
    {
        ModelProperty mp0 = new ModelProperty( "http://apache.org/maven/project/copy/fileset#property/dir", "target" );
        ModelProperty mp1 =
            new ModelProperty( "http://apache.org/maven/project/copy", "src" );
        assertFalse( mp1.isParentOfExcludingProperties(mp0));
    }     
}
