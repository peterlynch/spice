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

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.guice.bean.reflect.BeanProperties;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.Generics;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeConverterBinding;

/**
 * {@link TypeConverter} {@link Module} that converts Plexus formatted XML into the appropriate instances.
 */
public final class XmlTypeConverter
    extends AbstractMatcher<TypeLiteral<?>>
    implements Module, TypeConverter
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String CONVERSION_ERROR = "Cannot convert: \"%s\" to: %s";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private TypeConverterBinding[] otherConverterBindings;

    // ----------------------------------------------------------------------
    // Guice binding
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        // we're both matcher and converter
        binder.convertToTypes( this, this );
        binder.requestInjection( this );
    }

    // ----------------------------------------------------------------------
    // Guice setter
    // ----------------------------------------------------------------------

    /**
     * Records all the other {@link TypeConverterBinding}s registered with the {@link Injector}.
     * 
     * @param injector The injector
     */
    @Inject
    void recordOtherConverterBindings( final Injector injector )
    {
        final List<TypeConverterBinding> tempBindings = new ArrayList<TypeConverterBinding>();
        for ( final TypeConverterBinding b : injector.getTypeConverterBindings() )
        {
            // play safe: don't want to get into any sort of recursion!
            if ( !( b.getTypeConverter() instanceof XmlTypeConverter ) )
            {
                tempBindings.add( b );
            }
        }
        otherConverterBindings = tempBindings.toArray( new TypeConverterBinding[tempBindings.size()] );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean matches( final TypeLiteral<?> type )
    {
        // basic idea: we handle any conversion that the others don't
        for ( final TypeConverterBinding b : otherConverterBindings )
        {
            if ( b.getTypeMatcher().matches( type ) )
            {
                return false; // another converter can handle this
            }
        }
        return true;
    }

    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        try
        {
            final XmlPullParser parser = new MXParser();
            // we XMLize simple strings so we can convert types that accept string constructors, like File and URL
            parser.setInput( new StringReader( value.trim().startsWith( "<" ) ? value : "<_>" + value + "</_>" ) );
            return parse( parser, toType );
        }
        catch ( final Exception e )
        {
            throw new IllegalArgumentException( String.format( CONVERSION_ERROR, value, toType ), e );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Parses a sequence of XML elements and converts them to the given target type.
     * 
     * @param parser The XML parser
     * @param toType The target type
     * @return Converted instance of the target type
     */
    @SuppressWarnings( "unchecked" )
    private <T> T parse( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws Exception
    {
        // remember any custom implementation from containing element
        final String implementationName = parseImplementation( parser );
        final Class<?> rawType = toType.getRawType();

        if ( parser.next() == XmlPullParser.START_TAG )
        {
            if ( Properties.class.isAssignableFrom( rawType ) )
            {
                return (T) parseProperties( parser );
            }
            if ( Map.class.isAssignableFrom( rawType ) )
            {
                return (T) parseMap( parser, Generics.getTypeArgument( toType, 1 ) );
            }
            if ( Collection.class.isAssignableFrom( rawType ) )
            {
                return (T) parseCollection( parser, Generics.getTypeArgument( toType, 0 ) );
            }
            if ( rawType.isArray() )
            {
                return (T) parseArray( parser, Generics.getComponentType( toType ) );
            }
            return parseBean( parser, toType );
        }

        parser.require( XmlPullParser.TEXT, null, null );

        // see if the element containing this text declared any custom implementation
        final Class<T> clazz = (Class) loadImplementation( implementationName, rawType );
        return convertText( parser.getText(), rawType == clazz ? toType : TypeLiteral.get( clazz ) );
    }

    /**
     * Parses a sequence of XML elements and converts them to the appropriate {@link Properties} type.
     * 
     * @param parser The XML parser
     * @return Converted Properties instance
     */
    private Properties parseProperties( final XmlPullParser parser )
        throws Exception
    {
        final Properties properties = newImplementation( parser, Properties.class );
        // properties can be 'name-value' or 'value-name'
        while ( parser.next() != XmlPullParser.END_TAG )
        {
            parser.next();
            if ( "name".equals( parser.getName() ) )
            {
                final String name = parser.nextText();
                parser.next();
                properties.put( name, parser.nextText() );
            }
            else
            {
                final String value = parser.nextText();
                parser.next();
                properties.put( parser.nextText(), value );
            }
            parser.next();
        }
        return properties;
    }

    /**
     * Parses a sequence of XML elements and converts them to the appropriate {@link Map} type.
     * 
     * @param parser The XML parser
     * @return Converted Map instance
     */
    private <T> Map<String, T> parseMap( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws Exception
    {
        @SuppressWarnings( "unchecked" )
        final Map<String, T> map = newImplementation( parser, HashMap.class );
        while ( parser.next() != XmlPullParser.END_TAG )
        {
            map.put( parser.getName(), parse( parser, toType ) );
            parser.next();
        }
        return map;
    }

    /**
     * Parses a sequence of XML elements and converts them to the appropriate {@link Collection} type.
     * 
     * @param parser The XML parser
     * @return Converted Collection instance
     */
    private <T> Collection<T> parseCollection( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws Exception
    {
        @SuppressWarnings( "unchecked" )
        final Collection<T> collection = newImplementation( parser, ArrayList.class );
        while ( parser.next() != XmlPullParser.END_TAG )
        {
            collection.add( parse( parser, toType ) );
            parser.next();
        }
        return collection;
    }

    /**
     * Parses a sequence of XML elements and converts them to the appropriate array type.
     * 
     * @param parser The XML parser
     * @return Converted array instance
     */
    private Object parseArray( final XmlPullParser parser, final TypeLiteral<?> toType )
        throws Exception
    {
        // convert to a collection first then convert that into an array
        final Collection<?> collection = parseCollection( parser, toType );
        final Object array = Array.newInstance( toType.getRawType(), collection.size() );

        int i = 0;
        for ( final Object element : collection )
        {
            Array.set( array, i++, element );
        }

        return array;
    }

    /**
     * Parses a sequence of XML elements and converts them to the appropriate bean type.
     * 
     * @param parser The XML parser
     * @return Converted bean instance
     */
    private <T> T parseBean( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws Exception
    {
        @SuppressWarnings( "unchecked" )
        final Class<T> clazz = (Class) loadImplementation( parseImplementation( parser ), toType.getRawType() );
        if ( parser.next() == XmlPullParser.TEXT )
        {
            // we expect public string constructor
            final String text = parser.getText();
            parser.next();
            return newImplementation( clazz, text );
        }

        final T bean = newImplementation( clazz );

        // build map of all known bean properties belonging to the chosen implementation
        final Map<String, BeanProperty<Object>> propertyMap = new HashMap<String, BeanProperty<Object>>();
        for ( final BeanProperty<Object> property : new BeanProperties( clazz ) )
        {
            final String name = property.getName();
            if ( !propertyMap.containsKey( name ) )
            {
                propertyMap.put( name, property );
            }
        }

        while ( parser.getEventType() != XmlPullParser.END_TAG )
        {
            // update properties inside the current bean, guided by the cached map
            final BeanProperty<Object> property = propertyMap.get( parser.getName() );
            if ( property != null )
            {
                property.set( bean, parse( parser, property.getType() ) );
                parser.next();
                parser.next();
            }
            else
            {
                throw new XmlPullParserException( "Unknown bean property: " + parser.getName(), parser, null );
            }
        }

        return bean;
    }

    /**
     * Parses an XML element looking for the name of a custom implementation.
     * 
     * @param parser The XML parser
     * @return Name of the custom implementation; otherwise {@code null}
     */
    private static String parseImplementation( final XmlPullParser parser )
        throws XmlPullParserException
    {
        if ( parser.getEventType() == XmlPullParser.START_TAG )
        {
            return parser.getAttributeValue( null, "implementation" );
        }
        return null;
    }

    /**
     * Attempts to load the named implementation, uses default implementation if no name is given.
     * 
     * @param implementationName The optional implementation name
     * @param defaultClazz The default implementation type
     * @return Custom implementation type if one was given; otherwise default implementation type
     */
    private static Class<?> loadImplementation( final String implementationName, final Class<?> defaultClazz )
    {
        // fall-back to the default type?
        if ( null == implementationName )
        {
            return defaultClazz;
        }

        // TCCL allows surrounding container to influence class loading policy
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if ( tccl != null )
        {
            try
            {
                return tccl.loadClass( implementationName );
            }
            catch ( final ClassNotFoundException e ) // NOPMD
            {
                // drop through and try the peer class loader
            }
        }

        // assume custom type is in same class space as default
        final ClassLoader peer = defaultClazz.getClassLoader();
        if ( peer != null )
        {
            try
            {
                return peer.loadClass( implementationName );
            }
            catch ( final ClassNotFoundException e ) // NOPMD
            {
                // drop through and try the classic approach
            }
        }

        try
        {
            // last chance - standard class loading
            return Class.forName( implementationName );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new IllegalArgumentException( "Cannot load implementation: " + implementationName, e );
        }
    }

    /**
     * Creates an instance of the given implementation using the default constructor.
     * 
     * @param clazz The implementation type
     * @return Instance of given implementation
     */
    private static <T> T newImplementation( final Class<T> clazz )
    {
        try
        {
            return clazz.newInstance();
        }
        catch ( final Exception e )
        {
            throw new IllegalArgumentException( "Cannot create instance of: " + clazz, e );
        }
    }

    /**
     * Creates an instance of the given implementation using the given string, assumes a public string constructor.
     * 
     * @param clazz The implementation type
     * @param value The string argument
     * @return Instance of given implementation, constructed using the the given string
     */
    private static <T> T newImplementation( final Class<T> clazz, final String value )
    {
        try
        {
            return clazz.getConstructor( String.class ).newInstance( value );
        }
        catch ( final InvocationTargetException e )
        {
            throw new IllegalArgumentException( String.format( CONVERSION_ERROR, value, clazz ), e.getTargetException() );
        }
        catch ( final Exception e )
        {
            throw new IllegalArgumentException( String.format( CONVERSION_ERROR, value, clazz ), e );
        }
    }

    /**
     * Creates an instance of the implementation named in the current XML element, or the default if no name is given.
     * 
     * @param parser The XML parser
     * @param defaultClazz The default implementation type
     * @return Instance of custom implementation if one was given; otherwise instance of default type
     */
    @SuppressWarnings( "unchecked" )
    private static <T> T newImplementation( final XmlPullParser parser, final Class<T> defaultClazz )
        throws XmlPullParserException
    {
        return (T) newImplementation( loadImplementation( parseImplementation( parser ), defaultClazz ) );
    }

    /**
     * Converts the given string to the target type, using {@link TypeConverter}s registered with the {@link Injector}.
     * 
     * @param value The string value
     * @param toType The target type
     * @return Converted instance of the target type
     */
    @SuppressWarnings( "unchecked" )
    private <T> T convertText( final String value, final TypeLiteral<T> toType )
    {
        final Class<?> rawType = toType.getRawType();
        if ( rawType.isAssignableFrom( String.class ) )
        {
            return (T) value; // compatible type => no conversion needed
        }

        // use temporary Key to auto-box primitive types into their equivalent object types
        final TypeLiteral<?> boxedType = rawType.isPrimitive() ? Key.get( rawType ).getTypeLiteral() : toType;

        for ( final TypeConverterBinding b : otherConverterBindings )
        {
            if ( b.getTypeMatcher().matches( boxedType ) )
            {
                return (T) b.getTypeConverter().convert( value, toType );
            }
        }

        // last chance => try public string constructor?
        return (T) newImplementation( rawType, value );
    }
}
