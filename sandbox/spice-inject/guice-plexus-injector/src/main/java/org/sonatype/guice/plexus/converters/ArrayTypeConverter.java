package org.sonatype.guice.plexus.converters;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeConverter;

public final class ArrayTypeConverter
    implements TypeConverter, Module
{
    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        return null;
    }

    public void configure( Binder binder )
    {
        binder.convertToTypes( new AbstractMatcher<TypeLiteral<?>>()
        {
            public boolean matches( TypeLiteral<?> type )
            {
                return type.getRawType().isArray();
            }
        }, this );
    }
}
