package org.codehaus.plexus.component.configurator.converters.composite;

/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * Converter for <code>java.util.Properties</code>.
 * 
 * @author <a href="mailto:michal@codehaus.org">Michal Maczka</a>
 * @version $Id: MapConverter.java 7285 2008-04-14 20:27:40Z jdcasey $
 */
@SuppressWarnings( "rawtypes" )
public class MapConverter
    extends AbstractConfigurationConverter
{
    public boolean canConvert( final Class type )
    {
        return Map.class.isAssignableFrom( type ) && !Properties.class.isAssignableFrom( type );
    }

    @SuppressWarnings( "unchecked" )
    public Object fromConfiguration( final ConverterLookup converterLookup, final PlexusConfiguration configuration,
                                     final Class type, final Class baseType, final ClassLoader classLoader,
                                     final ExpressionEvaluator expressionEvaluator, final ConfigurationListener listener )
        throws ComponentConfigurationException
    {
        Object retValue;

        String expression = configuration.getValue( null );

        if ( expression == null )
        {
            expression = configuration.getAttribute( "default-value", null );
        }

        if ( expression == null )
        {
            final Map map = new TreeMap();

            final PlexusConfiguration[] children = configuration.getChildren();

            for ( final PlexusConfiguration child : children )
            {
                final String name = child.getName();

                map.put( name, fromExpression( child, expressionEvaluator ) );
            }
            retValue = map;
        }
        else
        {
            retValue = fromExpression( configuration, expressionEvaluator );
        }
        return retValue;
    }

}
