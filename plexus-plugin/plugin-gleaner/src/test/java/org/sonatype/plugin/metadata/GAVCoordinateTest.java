package org.sonatype.plugin.metadata;

import junit.framework.TestCase;

public class GAVCoordinateTest
    extends TestCase
{
    public void testSimples()
    {
        GAVCoordinate gav = null;

        gav = new GAVCoordinate( "g", "a", "v" );

        assertEquals( "g:a:v", gav.toCompositeForm() );

        gav = new GAVCoordinate( "g:a:v" );

        assertEquals( "g:a:v", gav.toCompositeForm() );
    }

    public void testAll()
    {
        GAVCoordinate gav = null;

        gav = new GAVCoordinate( "g", "a", "v", "c", "t" );

        assertEquals( "g:a:v:c:t", gav.toCompositeForm() );

        gav = new GAVCoordinate( "g", "a", "v", null, "t" );

        assertEquals( "g:a:v::t", gav.toCompositeForm() );

        gav = new GAVCoordinate( "g", "a", "v", "c", null );

        assertEquals( "g:a:v:c", gav.toCompositeForm() );

        gav = new GAVCoordinate( "g", "a", "v", null, null );

        assertEquals( "g:a:v", gav.toCompositeForm() );

        gav = new GAVCoordinate( "g:a:v:c:t" );

        assertEquals( "g:a:v:c:t", gav.toCompositeForm() );

        gav = new GAVCoordinate( "g:a:v::t" );

        assertEquals( "g:a:v::t", gav.toCompositeForm() );

        gav = new GAVCoordinate( "g:a:v:c" );

        assertEquals( "g:a:v:c", gav.toCompositeForm() );
    }

    public void testEquality()
    {
        GAVCoordinate gav1 = null;

        GAVCoordinate gav2 = null;

        gav1 = new GAVCoordinate( "g", "a", "v" );

        gav2 = new GAVCoordinate( "g", "a", "v", "", "" );

        assertEquals( gav1, gav2 );

        gav1 = new GAVCoordinate( "g", "a", "v" );

        gav2 = new GAVCoordinate( "g", "a", "v", null, null );

        assertEquals( gav1, gav2 );

        gav1 = new GAVCoordinate( "g", "a", "v" );

        gav2 = new GAVCoordinate( "g", "a", "v", null, "jar" );

        assertEquals( gav1, gav2 );

        gav1 = new GAVCoordinate( "g:a:v:c" );

        gav2 = new GAVCoordinate( "g", "a", "v", "c", "jar" );

        assertEquals( gav1, gav2 );

        gav1 = new GAVCoordinate( "g:a:v:c" );

        gav2 = new GAVCoordinate( "g", "a", "v", "c", "custom-packaging" );

        assertFalse( gav1.equals( gav2 ) );
    }
}
