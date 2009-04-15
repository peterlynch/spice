/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.jettytestsuite;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.util.IOUtil;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

public class KeyHandler
    extends AbstractHandler
{
    public static final long TEST_KEY_1 = 0xA3F9CCC081C4177DL;

    public static final long TEST_KEY_2 = 0x16E0CF8D6B0B9508L;

    private Map<String, String> keys = new LinkedHashMap<String, String>();

    public KeyHandler()
        throws IOException
    {
        addKey( TEST_KEY_1,
                IOUtil.toString( getClass().getResourceAsStream( "/com/sonatype/mercury/plexus/pgp/testkey1.txt" ) ) );
        addKey( TEST_KEY_2,
                IOUtil.toString( getClass().getResourceAsStream( "/com/sonatype/mercury/plexus/pgp/testkey2.txt" ) ) );
    }

    public void addKey( Long key, String content )
    {
        keys.put( ( "0x" + Long.toHexString( key ) ).toLowerCase(), content );
    }

    public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
        throws IOException, ServletException
    {
        String key = request.getParameter( "search" );
        String result = getStringFromKey( key );

        if ( result == null )
        {
            throw new IllegalArgumentException( "Invalid key received " + key );
        }

        response.setContentType( "text/html; charset=UTF-8" );
        response.setStatus( HttpServletResponse.SC_OK );
        response.setBufferSize( result.length() );
        response.setHeader( "Server", "sks_www/1.1.0" );

        response.getWriter().print( result );

        ( (Request) request ).setHandled( true );
    }

    protected String getStringFromKey( String key )
        throws IOException
    {
        String result = keys.get( key.toLowerCase() );
        return result;
    }
}
