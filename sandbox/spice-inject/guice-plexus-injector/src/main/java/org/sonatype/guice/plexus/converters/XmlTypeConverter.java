package org.sonatype.guice.plexus.converters;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverterBinding;

@Singleton
final class XmlTypeConverter
{
    private final List<TypeConverterBinding> converterBindings;

    @Inject
    public XmlTypeConverter( final Injector injector )
    {
        converterBindings = injector.getTypeConverterBindings();
    }

    public Collection<?> parseItems( final String value, final TypeLiteral<?> elementType )
    {
        final List<Object> items = new ArrayList<Object>();

        try
        {
            final XmlPullParser parser = new MXParser();
            parser.setInput( new StringReader( value ) );
            parser.nextTag();

            while ( parser.nextTag() == XmlPullParser.START_TAG )
            {
                final int depth = parser.getDepth();

                final StringBuilder buf = new StringBuilder();
                while ( parser.next() != XmlPullParser.END_TAG || parser.getDepth() > depth )
                {
                    buf.append( parser.getText() );
                }

                items.add( convertConstant( buf.toString(), elementType ) );
            }
        }
        catch ( final XmlPullParserException e )
        {
            throw new ProvisionException( e.toString() );
        }
        catch ( final IOException e )
        {
            throw new ProvisionException( e.toString() );
        }

        return items;
    }

    public TypeLiteral<?> getElementType( final TypeLiteral<?> containerType )
    {
        final Type refType = containerType.getType();
        if ( refType instanceof GenericArrayType )
        {
            return TypeLiteral.get( ( (GenericArrayType) refType ).getGenericComponentType() );
        }
        if ( refType instanceof ParameterizedType )
        {
            final Type paramType = ( (ParameterizedType) refType ).getActualTypeArguments()[0];
            if ( paramType instanceof WildcardType )
            {
                return TypeLiteral.get( ( (WildcardType) paramType ).getUpperBounds()[0] );
            }
            return TypeLiteral.get( paramType );
        }
        final Class<?> rawType = containerType.getRawType();
        if ( rawType.isArray() )
        {
            return TypeLiteral.get( rawType.getComponentType() );
        }
        return TypeLiteral.get( Object.class );
    }

    @SuppressWarnings( "unchecked" )
    private <T> T convertConstant( final String value, final TypeLiteral<T> toType )
    {
        for ( final TypeConverterBinding b : converterBindings )
        {
            if ( b.getTypeMatcher().matches( toType ) )
            {
                return (T) b.getTypeConverter().convert( value, toType );
            }
        }

        if ( toType.getRawType().isAssignableFrom( String.class ) )
        {
            return (T) value;
        }

        throw new IllegalArgumentException( "Cannot convert \"" + value + "\" to " + toType );
    }
}
