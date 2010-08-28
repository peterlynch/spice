package org.codehaus.plexus.component.configurator;

/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.io.StringReader;
import java.lang.annotation.ElementType;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.TypeAwareExpressionEvaluator;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.io.PlexusTools;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;

/**
 * @author <a href="mailto:michal@codehaus.org">Michal Maczka</a>
 * @version $Id: AbstractComponentConfiguratorTest.java 8622 2010-01-30 18:56:56Z bentmann $
 */
public abstract class AbstractComponentConfiguratorTest
    extends PlexusTestCase
{
    protected void configureComponent( final Object component, final ComponentDescriptor descriptor,
                                       final ClassRealm realm )
        throws Exception
    {
        final ComponentConfigurator cc = getComponentConfigurator();
        cc.configureComponent( component, descriptor.getConfiguration(), realm );
    }

    protected void configureComponent( final Object component, final ComponentDescriptor descriptor,
                                       final ClassRealm realm, final ExpressionEvaluator expressionEvaluator )
        throws Exception
    {
        final ComponentConfigurator cc = getComponentConfigurator();
        cc.configureComponent( component, descriptor.getConfiguration(), expressionEvaluator, realm );
    }

    protected abstract ComponentConfigurator getComponentConfigurator()
        throws Exception;

    public void testComponentConfigurator()
        throws Exception
    {
        final String xml =
            "<configuration>" + "  <boolean-value>true</boolean-value>" + "  <byte-value>64</byte-value>"
                + "  <short-value>-128</short-value>" + "  <int-value>-1</int-value>"
                + "  <float-value>1</float-value>" + "  <long-value>2</long-value>"
                + "  <double-value>3</double-value>" + "  <char-value>X</char-value>"
                + "  <string-value>foo</string-value>" + "  <file-value>test.txt</file-value>"
                + "  <uri-value>http://www.apache.org/</uri-value>"
                + "  <url-value>http://maven.apache.org/</url-value>" + "  <important-things>"
                + "    <important-thing><name>jason</name></important-thing>"
                + "    <important-thing><name>tess</name></important-thing>" + "  </important-things>"
                + "  <configuration>" + "      <name>jason</name>" + "  </configuration>" + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ConfigurableComponent component = new ConfigurableComponent();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        assertEquals( "check boolean value", true, component.getBooleanValue() );

        assertEquals( "check byte value", 64, component.getByteValue() );

        assertEquals( "check short value", -128, component.getShortValue() );

        assertEquals( "check integer value", -1, component.getIntValue() );

        assertEquals( "check float value", 1.0f, component.getFloatValue(), 0.001f );

        assertEquals( "check long value", 2L, component.getLongValue() );

        assertEquals( "check double value", 3.0, component.getDoubleValue(), 0.001 );

        assertEquals( 'X', component.getCharValue() );

        assertEquals( "foo", component.getStringValue() );

        assertEquals( new File( "test.txt" ), component.getFileValue() );

        assertEquals( new URI( "http://www.apache.org/" ), component.getUriValue() );

        assertEquals( new URL( "http://maven.apache.org/" ), component.getUrlValue() );

        final List list = component.getImportantThings();

        assertEquals( 2, list.size() );

        assertEquals( "jason", ( (ImportantThing) list.get( 0 ) ).getName() );

        assertEquals( "tess", ( (ImportantThing) list.get( 1 ) ).getName() );

        // Embedded Configuration

        final PlexusConfiguration c = component.getConfiguration();

        assertEquals( "jason", c.getChild( "name" ).getValue() );
    }

    public void testComponentConfiguratorWithAComponentThatProvidesSettersForConfiguration()
        throws Exception
    {
        final String xml =
            "<configuration>" + "  <int-value>0</int-value>" + "  <float-value>1</float-value>"
                + "  <long-value>2</long-value>" + "  <double-value>3</double-value>"
                + "  <string-value>foo</string-value>" + "  <important-things>"
                + "    <important-thing><name>jason</name></important-thing>"
                + "    <important-thing><name>tess</name></important-thing>" + "  </important-things>"
                + "  <configuration>" + "      <name>jason</name>" + "  </configuration>" + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithSetters component = new ComponentWithSetters();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        assertEquals( "check integer value", 0, component.getIntValue() );

        assertTrue( component.intValueSet );

        assertEquals( "check float value", 1.0f, component.getFloatValue(), 0.001f );

        assertTrue( component.floatValueSet );

        assertEquals( "check long value", 2L, component.getLongValue() );

        assertTrue( component.longValueSet );

        assertEquals( "check double value", 3.0, component.getDoubleValue(), 0.001 );

        assertTrue( component.doubleValueSet );

        assertEquals( "foo", component.getStringValue() );

        assertTrue( component.stringValueSet );

        final List list = component.getImportantThings();

        assertEquals( 2, list.size() );

        assertEquals( "jason", ( (ImportantThing) list.get( 0 ) ).getName() );

        assertEquals( "tess", ( (ImportantThing) list.get( 1 ) ).getName() );

        assertTrue( component.importantThingsValueSet );

        // Embedded Configuration

        final PlexusConfiguration c = component.getConfiguration();

        assertEquals( "jason", c.getChild( "name" ).getValue() );

        assertTrue( component.configurationValueSet );
    }

    public void testComponentConfigurationWhereFieldsToConfigureResideInTheSuperclass()
        throws Exception
    {
        final String xml =
            "<configuration>" + "  <name>jason</name>" + "  <address>bollywood</address>" + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final DefaultComponent component = new DefaultComponent();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        assertEquals( "jason", component.getName() );

        assertEquals( "bollywood", component.getAddress() );
    }

    public void testComponentConfigurationWhereFieldsAreCollections()
        throws Exception
    {
        final String xml =
            "<configuration>" + "  <vector>" + "    <important-thing>" + "       <name>life</name>"
                + "    </important-thing>" + "  </vector>" + "  <hashSet>" + "    <important-thing>"
                + "       <name>life</name>" + "    </important-thing>" + "  </hashSet>"
                + "   <list implementation=\"java.util.LinkedList\">" + "     <important-thing>"
                + "       <name>life</name>" + "    </important-thing>" + "  </list>" + "  <stringList>"
                + "    <something>abc</something>" + "    <somethingElse>def</somethingElse>" + "  </stringList>"
                + "   <set><something>abc</something></set>" + "   <sortedSet><something>abc</something></sortedSet>" +
                // TODO: implement List<int> etc..
                // "<intList>" +
                // "  <something>12</something>" +
                // "  <somethingElse>34</somethingElse>" +
                // "</intList>" +
                "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithCollectionFields component = new ComponentWithCollectionFields();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        final Vector vector = component.getVector();

        assertEquals( "life", ( (ImportantThing) vector.get( 0 ) ).getName() );

        assertEquals( 1, vector.size() );

        Set set = component.getHashSet();

        assertEquals( 1, set.size() );

        final Object[] setContents = set.toArray();

        assertEquals( "life", ( (ImportantThing) setContents[0] ).getName() );

        final List list = component.getList();

        assertEquals( list.getClass(), LinkedList.class );

        assertEquals( "life", ( (ImportantThing) list.get( 0 ) ).getName() );

        assertEquals( 1, list.size() );

        final List stringList = component.getStringList();

        assertEquals( "abc", (String) stringList.get( 0 ) );

        assertEquals( "def", (String) stringList.get( 1 ) );

        assertEquals( 2, stringList.size() );

        set = component.getSet();

        assertEquals( 1, set.size() );

        set = component.getSortedSet();

        assertEquals( 1, set.size() );
    }

    public void testComponentConfigurationWhereFieldsAreArrays()
        throws Exception
    {
        final String xml =
            "<configuration>" + "  <stringArray>" + "    <first-string>value1</first-string>"
                + "    <second-string>value2</second-string>" + "  </stringArray>" + "  <integerArray>"
                + "    <firstInt>42</firstInt>" + "    <secondInt>69</secondInt>" + "  </integerArray>"
                + "  <importantThingArray>" + "    <importantThing><name>Hello</name></importantThing>"
                + "    <importantThing><name>World!</name></importantThing>" + "  </importantThingArray>"
                + "  <objectArray>" + "    <java.lang.String>some string</java.lang.String>"
                + "    <importantThing><name>something important</name></importantThing>"
                + "    <whatever implementation='java.lang.Integer'>303</whatever>" + "  </objectArray>"
                + "  <urlArray>" + "    <url>http://foo.com/bar</url>" + "    <url>file://localhost/c:/windows</url>"
                + "  </urlArray>" + "  <fileArray>" + "    <file>c:/windows</file>"
                + "    <file>/usr/local/bin/foo.sh</file>" + "  </fileArray>" + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithArrayFields component = new ComponentWithArrayFields();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        final String[] stringArray = component.getStringArray();

        assertEquals( 2, stringArray.length );

        assertEquals( "value1", stringArray[0] );

        assertEquals( "value2", stringArray[1] );

        final Integer[] integerArray = component.getIntegerArray();

        assertEquals( 2, integerArray.length );

        assertEquals( new Integer( 42 ), integerArray[0] );

        assertEquals( new Integer( 69 ), integerArray[1] );

        final ImportantThing[] importantThingArray = component.getImportantThingArray();

        assertEquals( 2, importantThingArray.length );

        assertEquals( "Hello", importantThingArray[0].getName() );

        assertEquals( "World!", importantThingArray[1].getName() );

        final Object[] objectArray = component.getObjectArray();

        assertEquals( 3, objectArray.length );

        assertEquals( "some string", objectArray[0] );

        assertEquals( "something important", ( (ImportantThing) objectArray[1] ).getName() );

        assertEquals( new Integer( 303 ), objectArray[2] );

        final URL[] urls = component.getUrlArray();

        assertEquals( new URL( "http://foo.com/bar" ), urls[0] );

        assertEquals( new URL( "file://localhost/c:/windows" ), urls[1] );

        final File[] files = component.getFileArray();

        assertEquals( new File( "c:/windows" ), files[0] );

        assertEquals( new File( "/usr/local/bin/foo.sh" ), files[1] );
    }

    public void testComponentConfigurationWithCompositeFields()
        throws Exception
    {

        final String xml =
            "<configuration>"
                + "  <thing implementation=\"org.codehaus.plexus.component.configurator.ImportantThing\">"
                + "     <name>I am not abstract!</name>" + "  </thing>" + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithCompositeFields component = new ComponentWithCompositeFields();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        assertNotNull( component.getThing() );

        assertEquals( "I am not abstract!", component.getThing().getName() );

    }

    public void testInvalidComponentConfiguration()
        throws Exception
    {

        final String xml = "<configuration><goodStartElement>theName</badStopElement></configuration>";

        try
        {
            PlexusTools.buildConfiguration( "<Test-Invalid>", new StringReader( xml ) );

            fail( "Should have caused an error because of the invalid XML." );
        }
        catch ( final PlexusConfigurationException e )
        {
            // Error should be caught here.
        }
        catch ( final Exception e )
        {
            fail( "Should have caught the invalid plexus configuration exception." );
        }

    }

    public void testComponentConfigurationWithPropertiesFields()
        throws Exception
    {

        final String xml =
            "<configuration>" + "  <someProperties>" + "     <property>" + "        <name>firstname</name>"
                + "        <value>michal</value>" + "     </property>" + "     <property>"
                + "        <name>lastname</name>" + "        <value>maczka</value>" + "     </property>"
                + "  </someProperties>" + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithPropertiesField component = new ComponentWithPropertiesField();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        final Properties properties = component.getSomeProperties();

        assertNotNull( properties );

        assertEquals( "michal", properties.get( "firstname" ) );

        assertEquals( "maczka", properties.get( "lastname" ) );

    }

    public void testComponentConfigurationWithPropertiesFieldsWithExpression()
        throws Exception
    {

        final String xml =
            "<configuration>" + " <someProperties> ${injectedProperties} </someProperties>" + "</configuration>";

        final Properties propertiesInterpolated = new Properties();
        propertiesInterpolated.put( "firstname", "olivier" );
        propertiesInterpolated.put( "lastname", "lamy" );

        final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator()
        {
            public Object evaluate( final String expression )
            {
                return propertiesInterpolated;
            }

            public File alignToBaseDirectory( final File file )
            {
                return null;
            }
        };

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithPropertiesField component = new ComponentWithPropertiesField();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm, expressionEvaluator );

        final Properties properties = component.getSomeProperties();

        assertNotNull( properties );

        assertEquals( "olivier", properties.get( "firstname" ) );

        assertEquals( "lamy", properties.get( "lastname" ) );

    }

    public void testComponentConfigurationWithPropertiesFieldsWithExpressions()
        throws Exception
    {

        final String xml = "<configuration>" + "<someProperties>" //
            + "<property><name>${theName}</name><value>${theValue}</value></property>" //
            + "<property><name>empty</name></property>" //
            + "</someProperties>" + "</configuration>";

        final Properties values = new Properties();
        values.put( "${theName}", "test" );
        values.put( "${theValue}", "PASSED" );

        final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator()
        {
            public Object evaluate( final String expression )
            {
                return values.containsKey( expression ) ? values.get( expression ) : expression;
            }

            public File alignToBaseDirectory( final File file )
            {
                return null;
            }
        };

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithPropertiesField component = new ComponentWithPropertiesField();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm, expressionEvaluator );

        final Properties properties = component.getSomeProperties();

        assertNotNull( properties );

        assertEquals( "PASSED", properties.get( "test" ) );
        assertEquals( "", properties.get( "empty" ) );
    }

    public void testComponentConfigurationWithMapField()
        throws Exception
    {
        final String xml =
            "<configuration>" + "  <map>" + "     <firstName>Kenney</firstName>"
                + "     <lastName>Westerhof</lastName>" + "  </map>" + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithMapField component = new ComponentWithMapField();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        final Map map = component.getMap();

        assertNotNull( map );

        assertEquals( "Kenney", map.get( "firstName" ) );

        assertEquals( "Westerhof", map.get( "lastName" ) );

    }

    public void testComponentConfigurationWhereFieldIsBadArray()
        throws Exception
    {
        final String xml = "<configuration>" //
            + "  <integerArray><java.lang.String>string</java.lang.String></integerArray>" //
            + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithArrayFields component = new ComponentWithArrayFields();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        try
        {
            configureComponent( component, descriptor, realm );
            fail( "Configuration did not fail" );
        }
        catch ( final ComponentConfigurationException e )
        {
            // expected
            e.printStackTrace();
        }
    }

    public void testComponentConfigurationWhereFieldIsEnum()
        throws Exception
    {
        final String xml = "<configuration>" //
            + "  <simpleEnum>TYPE</simpleEnum>" //
            + "  <nestedEnum>ONE</nestedEnum>" //
            + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithEnumFields component = new ComponentWithEnumFields();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        assertEquals( ElementType.TYPE, component.getSimpleEnum() );

        assertEquals( ComponentWithEnumFields.NestedEnum.ONE, component.getNestedEnum() );
    }

    public void testComponentConfigurationWithAmbiguousExpressionValue()
        throws Exception
    {
        final String xml = "<configuration>" //
            + "  <address>${address}</address>" //
            + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final DefaultComponent component = new DefaultComponent();

        final ExpressionEvaluator expressionEvaluator = new TypeAwareExpressionEvaluator()
        {
            public Object evaluate( final String expression )
                throws ExpressionEvaluationException
            {
                return evaluate( expression, null );
            }

            public File alignToBaseDirectory( final File file )
            {
                return null;
            }

            public Object evaluate( final String expression, final Class<?> type )
                throws ExpressionEvaluationException
            {
                if ( String.class == type )
                {
                    return "PASSED";
                }
                else
                {
                    return Boolean.FALSE;
                }
            }
        };

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm, expressionEvaluator );

        assertEquals( "PASSED", component.getAddress() );
    }

    public void testComponentConfigurationWithPrimitiveValueConversion()
        throws Exception
    {
        final String xml = "<configuration>" //
            + "  <intValue>${primitive}</intValue>" //
            + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ConfigurableComponent component = new ConfigurableComponent();

        final ExpressionEvaluator expressionEvaluator = new TypeAwareExpressionEvaluator()
        {
            public Object evaluate( final String expression )
                throws ExpressionEvaluationException
            {
                return evaluate( expression, null );
            }

            public File alignToBaseDirectory( final File file )
            {
                return null;
            }

            public Object evaluate( final String expression, final Class<?> type )
                throws ExpressionEvaluationException
            {
                // java.lang.Short -> short -> int
                return new Short( (short) 23 );
            }
        };

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm, expressionEvaluator );

        assertEquals( 23, component.getIntValue() );
    }

    public void testComponentConfigurationWithEmptyContentForBasicField()
        throws Exception
    {
        final String xml = "<configuration>" //
            + "  <address></address>" //
            + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final DefaultComponent component = new DefaultComponent();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        assertEquals( null, component.getAddress() );
    }

    public void testComponentConfigurationWithEmptyContentForCompositeField()
        throws Exception
    {
        final String xml = "<configuration>" //
            + "  <bean/>" //
            + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithCompositeFields component = new ComponentWithCompositeFields();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        assertNotNull( component.getBean() );
    }

    public void testComponentConfigurationWithUnresolvedExpressionContentForCompositeFieldOfNonInstantiatableType()
        throws Exception
    {
        final String xml = "<configuration>" //
            + "  <thing>${null-valued-expression}</thing>" //
            + "</configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithCompositeFields component = new ComponentWithCompositeFields();

        final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator()
        {
            public Object evaluate( final String expression )
                throws ExpressionEvaluationException
            {
                return null;
            }

            public File alignToBaseDirectory( final File file )
            {
                return null;
            }
        };

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm, expressionEvaluator );

        assertEquals( null, component.getThing() );
    }

    public void testComponentConfiguratorFileNormalizesSeparator()
        throws Exception
    {
        final String xml =
            "<configuration><fileArray>" + "  <file>dir/test.txt</file>" + "  <file>dir\\test.txt</file>"
                + "</fileArray></configuration>";

        final PlexusConfiguration configuration = PlexusTools.buildConfiguration( "<Test>", new StringReader( xml ) );

        final ComponentWithArrayFields component = new ComponentWithArrayFields();

        final ComponentDescriptor descriptor = new ComponentDescriptor();

        descriptor.setRole( "role" );

        descriptor.setImplementation( component.getClass().getName() );

        descriptor.setConfiguration( configuration );

        final ClassWorld classWorld = new ClassWorld();

        final ClassRealm realm = classWorld.newRealm( "test", getClass().getClassLoader() );

        configureComponent( component, descriptor, realm );

        assertEquals( new File( "dir", "test.txt" ), component.getFileArray()[0] );
        assertEquals( new File( "dir", "test.txt" ), component.getFileArray()[1] );
    }

}
