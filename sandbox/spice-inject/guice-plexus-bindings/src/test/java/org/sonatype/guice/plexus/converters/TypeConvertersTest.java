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

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class TypeConvertersTest
    extends TestCase
{
    Module[] converterModules = { new DateTypeConverter(), new XmlTypeConverter() };

    @Override
    protected void setUp()
        throws Exception
    {
        Guice.createInjector( new AbstractModule()
        {
            private void bindConfig( final String name, final String value )
            {
                bindConstant().annotatedWith( Names.named( name ) ).to( value );
            }

            @Override
            protected void configure()
            {
                bindConfig( "Date1", "2005-10-06 2:22:55.1 PM" );
                bindConfig( "Date2", "2005-10-06 2:22:55PM" );

                bindConfig( "Array1", "<items><item>1</item><item>2</item><item>3</item></items>" );
                bindConfig( "Array2", "<items><item>4</item><item>5</item><item>6</item></items>" );
                bindConfig( "Array3",
                            "<as><a><bs><b>1</b><b>2</b></bs></a><a><bs><b>3</b><b>4</b></bs></a><a><bs><b>5</b><b>6</b></bs></a></as>" );

                bindConfig( "Collection1",
                            "<animals><animal>cat</animal><animal>dog</animal><animal>aardvark</animal></animals>" );
                bindConfig( "Collection2",
                            "<as><a><bs><b>1</b><b>2</b></bs></a><a><bs><b>3</b><b>4</b></bs></a><a><bs><b>5</b><b>6</b></bs></a></as>" );

                bindConfig( "Map", "<entries><key1>value1</key1><key2>value2</key2></entries>" );

                bindConfig( "Properties", "<properties><property><name>key1</name><value>value1</value></property>"
                    + "<property><value>value2</value><name>key2</name></property></properties>" );

                bindConfig( "File", "temp/readme.txt" );
                bindConfig( "URL", "http://www.sonatype.org" );

                bindConfig( "PersonBean", "<person><firstName>John</firstName><lastName>Smith</lastName></person>" );

                bindConfig( "UrlBean", "<url>http://www.sonatype.org/</url>" );

                for ( final Module m : converterModules )
                {
                    install( m );
                }
            }
        } ).injectMembers( this );
    }

    @Inject
    @Named( "Date1" )
    String dateString1;

    @Inject
    @Named( "Date1" )
    Date date1;

    @Inject
    @Named( "Date2" )
    String dateString2;

    @Inject
    @Named( "Date2" )
    Date date2;

    @Inject
    @Named( "Array1" )
    String[] array1;

    @Inject
    @Named( "Array2" )
    Integer[] array2;

    @Inject
    @Named( "Array3" )
    Integer[][] array3;

    @Inject
    @Named( "Collection1" )
    Collection<?> collection1;

    @Inject
    @Named( "Collection2" )
    Collection<Collection<Integer>> collection2;

    @Inject
    @Named( "Map" )
    Map<?, ?> map;

    @Inject
    @Named( "Properties" )
    Properties properties;

    @Inject
    @Named( "File" )
    File file;

    @Inject
    @Named( "URL" )
    URL url;

    static class Person1
    {
        public String firstName;

        public String lastName;
    }

    static class Person2
    {
        String m_firstName;

        String m_lastName;

        public void setFirstName( String firstName )
        {
            m_firstName = firstName;
        }

        public void setLastName( String lastName )
        {
            m_lastName = lastName;
        }
    }

    @Inject
    @Named( "PersonBean" )
    Person1 person1;

    @Inject
    @Named( "PersonBean" )
    Person2 person2;

    @Inject
    @Named( "UrlBean" )
    URL urlBean;

    @SuppressWarnings( { "boxing", "unchecked" } )
    public void testTypeConversions()
    {
        assertEquals( dateString1, new SimpleDateFormat( "yyyy-MM-dd h:mm:ss.S a" ).format( date1 ) );
        assertEquals( dateString2, new SimpleDateFormat( "yyyy-MM-dd h:mm:ssa" ).format( date2 ) );

        assertTrue( Arrays.equals( new String[] { "1", "2", "3" }, array1 ) );
        assertTrue( Arrays.equals( new Integer[] { 4, 5, 6 }, array2 ) );

        assertTrue( Arrays.deepEquals( new Integer[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } }, array3 ) );

        assertEquals( Arrays.asList( "cat", "dog", "aardvark" ), collection1 );

        assertEquals( Arrays.asList( Arrays.asList( 1, 2 ), Arrays.asList( 3, 4 ), Arrays.asList( 5, 6 ) ), collection2 );

        final HashMap<String, String> testMap = new HashMap<String, String>();
        testMap.put( "key1", "value1" );
        testMap.put( "key2", "value2" );

        assertEquals( testMap, map );
        assertEquals( testMap, properties );

        assertEquals( "temp/readme.txt", file.getPath().replace( '\\', '/' ) );
        assertEquals( "www.sonatype.org", url.getHost() );

        assertEquals( "John", person1.firstName );
        assertEquals( "Smith", person1.lastName );

        assertEquals( "John", person2.m_firstName );
        assertEquals( "Smith", person2.m_lastName );

        assertEquals( "www.sonatype.org", urlBean.getHost() );
    }
}
