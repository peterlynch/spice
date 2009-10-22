package org.sonatype.guice.plexus.converters;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverterBinding;

@Singleton
final class XmlTypeConverter
{
    private static final TypeLiteral<Object> OBJECT_TYPE_LITERAL = TypeLiteral.get( Object.class );

    private final TypeConverterBinding[] converterBindings;

    @Inject
    XmlTypeConverter( final Injector injector )
    {
        final List<TypeConverterBinding> bindings = injector.getTypeConverterBindings();
        converterBindings = bindings.toArray( new TypeConverterBinding[bindings.size()] );
    }

    @SuppressWarnings( "unchecked" )
    public <T> T parse( final XmlPullParser parser, final TypeLiteral<T> toType )
        throws XmlPullParserException, IOException
    {
        if ( parser.next() == XmlPullParser.START_TAG )
        {
            final Class<?> rawType = toType.getRawType();
            if ( Map.class.isAssignableFrom( rawType ) )
            {
                return (T) parseMap( parser, toType );
            }
            if ( Collection.class.isAssignableFrom( rawType ) )
            {
                return (T) parseCollection( parser, toType );
            }
            if ( rawType.isArray() )
            {
                return (T) parseArray( parser, toType );
            }
        }

        parser.require( XmlPullParser.TEXT, null, null );
        return convertText( parser.getText(), toType );
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

    private static TypeLiteral<?> getMapComponentType( final Type mapType )
    {
        return mapType instanceof ParameterizedType ? getTypeArgument( mapType, 1 ) : OBJECT_TYPE_LITERAL;
    }

    private static TypeLiteral<?> getCollectionComponentType( final Type collectionType )
    {
        return collectionType instanceof ParameterizedType ? getTypeArgument( collectionType, 0 ) : OBJECT_TYPE_LITERAL;
    }

    private static TypeLiteral<?> getArrayComponentType( final TypeLiteral<?> arrayType )
    {
        final Type refType = arrayType.getType();
        if ( refType instanceof GenericArrayType )
        {
            return expandType( ( (GenericArrayType) refType ).getGenericComponentType() );
        }
        return TypeLiteral.get( arrayType.getRawType().getComponentType() );
    }

    private static TypeLiteral<?> getParamType( final Type type, final int index )
    {
        return type instanceof ParameterizedType ? expandType( ( (ParameterizedType) type ).getActualTypeArguments()[index] ) : OBJECT_TYPE_LITERAL;
    }

    private static TypeLiteral<?> expandType( final Type type )
    {
        return TypeLiteral.get( type instanceof WildcardType ? ( (WildcardType) type ).getUpperBounds()[0] : type );
    }
}
