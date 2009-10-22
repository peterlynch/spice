package org.sonatype.guice.plexus.converters;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeConverterBinding;

public final class XmlTypeConverter
    implements TypeConverter, Module
{
    private static final TypeLiteral<Object> OBJECT_TYPE_LITERAL = TypeLiteral.get( Object.class );

    List<TypeConverterBinding> converterBindings;

    @Inject
    @SuppressWarnings( "unused" )
    private void recordConverterBindings( final Injector injector )
    {
        converterBindings = new ArrayList<TypeConverterBinding>();
        for ( TypeConverterBinding b : injector.getTypeConverterBindings() )
        {
            if ( !( b.getTypeConverter() instanceof XmlTypeConverter ) )
            {
                converterBindings.add( b );
            }
        }
    }

    public Object convert( String value, TypeLiteral<?> toType )
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
            throw new ProvisionException( "I/O error converting \"" + value + "\" to " + toType, e );
        }
    }

    public void configure( final Binder binder )
    {
        binder.convertToTypes( new AbstractMatcher<TypeLiteral<?>>()
        {
            public boolean matches( final TypeLiteral<?> type )
            {
                for ( final TypeConverterBinding b : converterBindings )
                {
                    if ( b.getTypeMatcher().matches( type ) )
                    {
                        return false;
                    }
                }
                return true;
            }
        }, this );

        binder.requestInjection( this );
    }

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
                return (T) parseMap( parser, getTypeArgument( toType, 1 ) );
            }
            if ( Collection.class.isAssignableFrom( rawType ) )
            {
                return (T) parseCollection( parser, getTypeArgument( toType, 0 ) );
            }
            if ( rawType.isArray() )
            {
                return (T) parseArray( parser, getComponentType( toType ) );
            }
            try
            {
                return parseBean( parser, toType );
            }
            catch ( Exception e )
            {
                throw new ProvisionException( "Error parsing bean type " + toType, e );
            }
        }

        parser.require( XmlPullParser.TEXT, null, null );
        return convertText( parser.getText(), toType );
    }

    private Properties parseProperties( final XmlPullParser parser )
        throws XmlPullParserException, IOException
    {
        final Properties properties = new Properties();
        while ( parser.nextTag() != XmlPullParser.END_TAG )
        {
            parser.nextTag();
            if ( "name".equals( parser.getName() ) )
            {
                final String name = parser.nextText();
                parser.nextTag();
                properties.put( name, parser.nextText() );
            }
            else
            {
                final String value = parser.nextText();
                parser.nextTag();
                properties.put( parser.nextText(), value );
            }
            parser.nextTag();
        }
        return properties;
    }

    private <T> Map<String, T> parseMap( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws XmlPullParserException, IOException
    {
        final Map<String, T> map = new HashMap<String, T>();
        while ( parser.nextTag() != XmlPullParser.END_TAG )
        {
            map.put( parser.getName(), parse( parser, toType ) );
            parser.nextTag();
        }
        return map;
    }

    private <T> Collection<T> parseCollection( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws XmlPullParserException, IOException
    {
        final List<T> collection = new ArrayList<T>();
        while ( parser.nextTag() != XmlPullParser.END_TAG )
        {
            collection.add( parse( parser, toType ) );
            parser.nextTag();
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
        final Class<?> rawType = toType.getRawType();
        if ( parser.next() == XmlPullParser.TEXT )
        {
            @SuppressWarnings( "unchecked" )
            final Constructor<T> ctor = (Constructor<T>) rawType.getConstructor( String.class );
            return ctor.newInstance( parser.getText() );
        }

        @SuppressWarnings( "unchecked" )
        final T bean = (T) rawType.newInstance();
        while ( parser.getEventType() != XmlPullParser.END_TAG )
        {
            final String name = parser.getName();
            try
            {
                final Field f = rawType.getField( name );
                f.set( bean, parse( parser, TypeLiteral.get( f.getGenericType() ) ) );
            }
            catch ( final NoSuchFieldException e )
            {
                final Method m =
                    rawType.getMethod( "set" + Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 ),
                                       String.class );
                m.invoke( bean, parse( parser, TypeLiteral.get( m.getGenericParameterTypes()[0] ) ) );
            }
            parser.nextTag();
            parser.nextTag();
        }
        return bean;
    }

    @SuppressWarnings( "unchecked" )
    private <T> T convertText( final String value, final TypeLiteral<T> toType )
    {
        if ( toType.getRawType().isAssignableFrom( String.class ) )
        {
            return (T) value;
        }

        for ( final TypeConverterBinding b : converterBindings )
        {
            if ( b.getTypeMatcher().matches( toType ) )
            {
                return (T) b.getTypeConverter().convert( value, toType );
            }
        }

        throw new IllegalArgumentException( "Cannot convert \"" + value + "\" to " + toType );
    }

    /**
     * Extracts a type argument from a generic type, for example {@code String} from {@code List<String>}.
     * 
     * @param genericType The generic type
     * @param index The type argument index
     * @return Selected type argument
     */
    private static TypeLiteral<?> getTypeArgument( final TypeLiteral<?> genericType, final int index )
    {
        final Type type = genericType.getType();
        if ( type instanceof ParameterizedType )
        {
            return flattenType( ( (ParameterizedType) type ).getActualTypeArguments()[index] );
        }
        return OBJECT_TYPE_LITERAL;
    }

    private static TypeLiteral<?> getComponentType( final TypeLiteral<?> genericType )
    {
        final Type type = genericType.getType();
        if ( type instanceof GenericArrayType )
        {
            return flattenType( ( (GenericArrayType) type ).getGenericComponentType() );
        }
        return TypeLiteral.get( genericType.getRawType().getComponentType() );
    }

    private static TypeLiteral<?> flattenType( final Type type )
    {
        return TypeLiteral.get( type instanceof WildcardType ? ( (WildcardType) type ).getUpperBounds()[0] : type );
    }
}
