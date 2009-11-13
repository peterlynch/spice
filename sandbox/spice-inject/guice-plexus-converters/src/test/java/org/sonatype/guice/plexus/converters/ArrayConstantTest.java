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
package org.sonatype.guice.plexus.converters;

import java.util.Arrays;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class ArrayConstantTest
    extends TestCase
{
    @Override
    protected void setUp()
        throws Exception
    {
        Guice.createInjector( new AbstractModule()
        {
            private void bind( final String name, final String value )
            {
                bindConstant().annotatedWith( Names.named( name ) ).to( value );
            }

            @Override
            protected void configure()
            {
                bind( "Empty", "<items/>" );
                bind( "Text", "<items><item>1</item><item>2</item><item>3</item></items>" );
                bind( "Numbers", "<items><item>4</item><item>5</item><item>6</item></items>" );
                bind( "Multi", "<as><a><bs><b>1</b><b>2</b></bs></a><a><bs><b>3</b><b>4</b></bs></a>"
                    + "<a><bs><b>5</b><b>6</b></bs></a></as>" );

                install( new XmlTypeConverter() );
            }
        } ).injectMembers( this );
    }

    @Inject
    @Named( "Empty" )
    char[] emptyArray;

    @Inject
    @Named( "Text" )
    String[] textArray;

    @Inject
    @Named( "Numbers" )
    int[] numbersArray;

    @Inject
    @Named( "Multi" )
    Integer[][] multiArray1;

    @Inject
    @Named( "Multi" )
    double[][] multiArray2;

    @SuppressWarnings( "boxing" )
    public void testTypeConversions()
    {
        assertEquals( 0, emptyArray.length );
        assertTrue( Arrays.equals( new String[] { "1", "2", "3" }, textArray ) );
        assertTrue( Arrays.equals( new int[] { 4, 5, 6 }, numbersArray ) );
        assertTrue( Arrays.deepEquals( new Integer[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } }, multiArray1 ) );
        assertTrue( Arrays.deepEquals( new double[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } }, multiArray2 ) );
    }
}
