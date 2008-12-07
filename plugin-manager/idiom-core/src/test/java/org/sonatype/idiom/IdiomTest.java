package org.sonatype.idiom;

import org.codehaus.plexus.PlexusTestCase;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class IdiomTest
    extends PlexusTestCase
{
    public void testIdiom()
        throws Exception
    {
        Idiom i = (Idiom) lookup( Idiom.class );
        assertNotNull( i );        
        i.execute( "confluence" );
    }   
}
