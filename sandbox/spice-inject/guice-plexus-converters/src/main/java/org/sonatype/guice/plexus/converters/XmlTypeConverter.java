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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.guice.bean.reflect.Generics;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
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
            try
            {
                return parseBean( parser, toType );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException( "Error parsing bean type " + toType, e );
            }
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

    private <T> T[] parseArray( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws XmlPullParserException, IOException
    {
        final Collection<T> collection = parseCollection( parser, toType );

        @SuppressWarnings( "unchecked" )
        final T[] array = (T[]) Array.newInstance( toType.getRawType(), collection.size() );
        collection.toArray( array );

        return array;
    }

    private <T> T parseBean( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws Exception
    {
        @SuppressWarnings( "unchecked" )
        final Class<T> rawType = (Class) toType.getRawType();
        final T bean = createImplementation( parser, rawType );

        while ( parser.getEventType() != XmlPullParser.END_TAG ) // TODO: re-use BeanProperties class?
        {
            final String name = parser.getName();
            try
            {
                final Field f = rawType.getField( name );
                f.set( bean, parse( parser, TypeLiteral.get( f.getGenericType() ) ) );
            }
            catch ( final NoSuchFieldException e )
            {
                final String setter = "set" + Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 );
                final Method m = rawType.getMethod( setter, String.class ); // TODO: other styles and types?
                m.invoke( bean, parse( parser, TypeLiteral.get( m.getGenericParameterTypes()[0] ) ) );
            }
            parser.next();
            parser.next();
        }
        return bean;
    }

    @SuppressWarnings( "unchecked" )
    private <T> T createImplementation( final XmlPullParser parser, final Class<T> defaultImplementation )
    {
        Class<T> clazz = defaultImplementation;
        try
        {
            final String implementationName = parser.getAttributeValue( null, "implementation" );
            if ( implementationName != null )
            {
                ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                if ( null == tccl )
                {
                    tccl = clazz.getClassLoader();
                }
                clazz = (Class) tccl.loadClass( implementationName );
            }

            if ( parser.next() == XmlPullParser.TEXT )
            {
                final String text = parser.getText();
                parser.next();
                if ( text.length() > 0 )
                {
                    return clazz.getDeclaredConstructor( String.class ).newInstance( text );
                }
            }

            return clazz.newInstance();
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e.toString() );
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
