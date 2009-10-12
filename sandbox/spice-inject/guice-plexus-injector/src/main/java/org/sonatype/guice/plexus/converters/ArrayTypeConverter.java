package org.sonatype.guice.plexus.converters;

import java.lang.reflect.Array;
import java.util.Collection;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeConverter;

public final class ArrayTypeConverter
    implements TypeConverter, Module
{
    @Inject
    private XmlTypeConverter xmlTypeConverter;

    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        final TypeLiteral<?> elementType = xmlTypeConverter.getElementType( toType );
        final Collection<?> items = xmlTypeConverter.parseItems( value, elementType );
        final Object array = Array.newInstance( elementType.getRawType(), items.size() );

        System.arraycopy( items.toArray(), 0, array, 0, items.size() );

        return array;
    }

    public void configure( final Binder binder )
    {
        binder.convertToTypes( new AbstractMatcher<TypeLiteral<?>>()
        {
            public boolean matches( final TypeLiteral<?> type )
            {
                return type.getRawType().isArray();
            }
        }, this );

        binder.requestInjection( this );
    }
}
