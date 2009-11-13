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

import java.io.IOException;
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
            parser.setInput( new StringReader( value.indexOf( '<' ) >= 0 ? value : "<_>" + value + "</_>" ) );
            return parse( parser, toType );
        }
        catch ( final XmlPullParserException e )
        {
            throw new IllegalArgumentException( "Cannot parse \"" + value + "\" as " + toType, e );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException( "I/O error converting \"" + value + "\" to " + toType, e );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private <T> T parse( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws XmlPullParserException, IOException
    {
        if ( parser.next() == XmlPullParser.START_TAG )
        {
            final Class<?> rawType = toType.getRawType();
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
        return convertText( parser.getText(), toType );
    }

    private Properties parseProperties( final XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        final Properties properties = createImplementation( parser, Properties.class );
        while ( parser.getEventType() != XmlPullParser.END_TAG )
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
            parser.next();
        }
        return properties;
    }

    private <T> Map<String, T> parseMap( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws XmlPullParserException, IOException
    {
        @SuppressWarnings( "unchecked" )
        final Map<String, T> map = createImplementation( parser, HashMap.class );
        while ( parser.getEventType() != XmlPullParser.END_TAG )
        {
            map.put( parser.getName(), parse( parser, toType ) );
            parser.next();
            parser.next();
        }
        return map;
    }

    private <T> Collection<T> parseCollection( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws XmlPullParserException, IOException
    {
        @SuppressWarnings( "unchecked" )
        final List<T> collection = createImplementation( parser, ArrayList.class );
        while ( parser.getEventType() != XmlPullParser.END_TAG )
        {
            collection.add( parse( parser, toType ) );
            parser.next();
            parser.next();
        }
        return collection;
    }

    private Object parseArray( final XmlPullParser parser, final TypeLiteral<?> toType )
        throws XmlPullParserException, IOException
    {
        final Class<?> componentType = toType.getRawType();
        final TypeLiteral<?> boxedType = componentType.isPrimitive() ? Key.get( toType ).getTypeLiteral() : toType;
        final Collection<?> collection = parseCollection( parser, boxedType );

        int i = 0;
        final Object array = Array.newInstance( componentType, collection.size() );
        for ( Object element : collection )
        {
            Array.set( array, i++, element );
        }

        return array;
    }

    private <T> T parseBean( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws XmlPullParserException, IOException
    {
        @SuppressWarnings( "unchecked" )
        final T bean = createImplementation( parser, (Class<T>) toType.getRawType() );

        final Map<String, BeanProperty<Object>> propertyMap = new HashMap<String, BeanProperty<Object>>();
        for ( final BeanProperty<Object> property : new BeanProperties( bean.getClass() ) )
        {
            final String name = property.getName();
            if ( !propertyMap.containsKey( name ) )
            {
                propertyMap.put( name, property );
            }
        }

        while ( parser.getEventType() != XmlPullParser.END_TAG )
        {
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

    @SuppressWarnings( "unchecked" )
    private <T> T createImplementation( final XmlPullParser parser, final Class<T> defaultImplementation )
        throws XmlPullParserException, IOException
    {
        final Class<T> clazz;

        final String implementationName = parser.getAttributeValue( null, "implementation" );
        if ( null == implementationName )
        {
            clazz = defaultImplementation;
        }
        else
        {
            try
            {
                ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                if ( null == tccl )
                {
                    tccl = defaultImplementation.getClassLoader();
                }
                clazz = (Class) tccl.loadClass( implementationName );
            }
            catch ( final ClassNotFoundException e )
            {
                throw new RuntimeException( "Unable to load implementation " + implementationName, e );
            }
        }

        try
        {
            if ( parser.next() == XmlPullParser.TEXT )
            {
                final String text = parser.getText();
                parser.next();
                if ( text.length() > 0 )
                {
                    try
                    {
                        return clazz.getConstructor( String.class ).newInstance( text );
                    }
                    catch ( NoSuchMethodException e ) // NOPMD
                    {
                        // drop through and try the default constructor
                    }
                }
            }
            return clazz.newInstance();
        }
        catch ( InvocationTargetException e )
        {
            throw new RuntimeException( "Unable to create instance of " + clazz, e.getCause() );
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( "Unable to create instance of " + clazz, e );
        }
        catch ( InstantiationException e )
        {
            throw new RuntimeException( "Unable to create instance of " + clazz, e );
        }
    }

    @SuppressWarnings( "unchecked" )
    private <T> T convertText( final String value, final TypeLiteral<T> toType )
    {
        if ( toType.getRawType().isAssignableFrom( String.class ) )
        {
            return (T) value; // no need for any conversion
        }

        for ( final TypeConverterBinding b : otherConverterBindings )
        {
            if ( b.getTypeMatcher().matches( toType ) )
            {
                return (T) b.getTypeConverter().convert( value, toType );
            }
        }

        throw new IllegalArgumentException( "Cannot convert \"" + value + "\" to " + toType );
    }
}
