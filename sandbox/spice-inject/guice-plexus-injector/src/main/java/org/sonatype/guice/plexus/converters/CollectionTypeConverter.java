package org.sonatype.guice.plexus.converters;

import java.util.Collection;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeConverter;

public final class CollectionTypeConverter
    implements TypeConverter
{
    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        return null;
    }

    public void configure( final Binder binder )
    {
        binder.convertToTypes( new AbstractMatcher<TypeLiteral<?>>()
        {
            public boolean matches( final TypeLiteral<?> type )
            {
                return Collection.class.isAssignableFrom( type.getRawType() );
            }
        }, this );
    }
}
