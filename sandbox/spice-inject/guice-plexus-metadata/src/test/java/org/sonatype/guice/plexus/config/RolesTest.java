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
package org.sonatype.guice.plexus.config;

import java.lang.annotation.Annotation;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.annotations.RequirementImpl;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;

public class RolesTest
    extends TestCase
{
    private static final TypeLiteral<Object> OBJECT_LITERAL = TypeLiteral.get( Object.class );

    private static final TypeLiteral<String> STRING_LITERAL = TypeLiteral.get( String.class );

    private static final Annotation OBJECT_ROLE_HINT = Names.named( "java.lang.Object" );

    private static final Annotation OBJECT_FOO_ROLE_HINT = Names.named( "java.lang.Object-foo" );

    public void testDefaultRoleHintKeys()
    {
        assertEquals( OBJECT_ROLE_HINT, Roles.roleHint( Object.class, null ) );
        assertEquals( OBJECT_ROLE_HINT, Roles.roleHint( Object.class, "" ) );
        assertEquals( OBJECT_ROLE_HINT, Roles.roleHint( Object.class, "default" ) );
        assertEquals( OBJECT_ROLE_HINT, Roles.roleHint( component( "" ) ) );
        assertEquals( OBJECT_ROLE_HINT, Roles.roleHint( component( "default" ) ) );
    }

    public void testRoleHintKeys()
    {
        assertEquals( OBJECT_FOO_ROLE_HINT, Roles.roleHint( Object.class, "foo" ) );
        assertEquals( OBJECT_FOO_ROLE_HINT, Roles.roleHint( component( "foo" ) ) );
    }

    public void testRoleAnalysis()
    {
        assertEquals( STRING_LITERAL, Roles.getRole( requirement( String.class ), OBJECT_LITERAL ) );
        assertEquals( STRING_LITERAL, Roles.getRole( requirement( Object.class ), STRING_LITERAL ) );

        assertEquals( STRING_LITERAL, Roles.getRole( requirement( Object.class ),
                                                     TypeLiteral.get( Types.listOf( String.class ) ) ) );

        assertEquals( STRING_LITERAL, Roles.getRole( requirement( Object.class ),
                                                     TypeLiteral.get( Types.mapOf( Object.class, String.class ) ) ) );
    }

    private static Component component( final String hint )
    {
        return new ComponentImpl( Object.class, hint, "per-lookup" );
    }

    private static Requirement requirement( final Class<?> role )
    {
        return new RequirementImpl( role, true );
    }
}
