package org.sonatype.guice.plexus.converters;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeConverter;

public final class DateTypeConverter
    implements TypeConverter, Module
{
    private final static DateFormat[] dateFormats =
        { new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss.S a" ), new SimpleDateFormat( "yyyy-MM-dd hh:mm:ssa" ),
            new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" ), new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) };

    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        for ( final DateFormat f : dateFormats )
        {
            try
            {
                return f.parse( value );
            }
            catch ( final ParseException e )
            {
                // try next format...
            }
        }
        throw new RuntimeException( "Unparseable date:" + value );
    }

    public void configure( final Binder binder )
    {
        binder.convertToTypes( new AbstractMatcher<TypeLiteral<?>>()
        {
            public boolean matches( final TypeLiteral<?> type )
            {
                return Date.class.isAssignableFrom( type.getRawType() );
            }
        }, this );
    }
}
