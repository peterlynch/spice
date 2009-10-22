package org.sonatype.guice.plexus.converters;

import java.io.StringReader;
import java.util.Map;

import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.TypeConverter;

public final class MapTypeConverter
    implements TypeConverter, Module
{
    @Inject
    private XmlTypeConverter xmlTypeConverter;

    public Object convert( final String value, final TypeLiteral<?> toType )
    {
        try
        {
            final XmlPullParser parser = new MXParser();
            parser.setInput( new StringReader( value ) );
            return xmlTypeConverter.parse( parser, toType );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public void configure( final Binder binder )
    {
        binder.convertToTypes( new AbstractMatcher<TypeLiteral<?>>()
        {
            public boolean matches( final TypeLiteral<?> type )
            {
                return Map.class.isAssignableFrom( type.getRawType() );
            }
        }, this );

        binder.requestInjection( this );
    }
}
