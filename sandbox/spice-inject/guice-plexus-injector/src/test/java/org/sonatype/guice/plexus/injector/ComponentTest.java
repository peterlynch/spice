package org.sonatype.guice.plexus.injector;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Requirement;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

/**
 * TODO: replace with proper tests! :)
 */
public class ComponentTest
    extends TestCase
{
    @Inject
    Injector injector;

    @Override
    protected void setUp()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Integer.class ).toInstance( 10 );
                bind( String.class ).toInstance( "!" );
                bind( String.class ).annotatedWith( Names.named( "foo" ) ).toInstance( "F" );
                bind( String.class ).annotatedWith( Names.named( "wibble" ) ).toInstance( "W" );
                bind( String.class ).annotatedWith( Names.named( "zzz" ) ).toInstance( "Z" );
                bind( new TypeLiteral<List<String>>()
                {
                } ).toInstance( Collections.singletonList( "HELLO" ) );
                bind( new TypeLiteral<Map<String, String>>()
                {
                } ).toInstance( Collections.singletonMap( "TEST", "CASE" ) );
                bindListener( Matchers.any(), new PlexusAnnotationListener() );
            }
        } ).injectMembers( this );
    }

    private static class Component
    {
        @Requirement
        private String a;

        @Requirement( role = Integer.class )
        private int b;

        @Requirement( hint = "foo" )
        private String c;

        @Requirement
        private List<String> d1;

        @Requirement
        private Map<String, String> d2;

        @Requirement( role = String.class, hints = { "wibble", "zzz", "" } )
        private List<?> e;

        @Requirement( role = String.class, hint = "foo" )
        private Map<String, ?> f;

        @Requirement( role = String.class, hints = { "wibble", "zzz", "" } )
        private Map<String, ?> g;

        @Override
        public String toString()
        {
            StringBuilder buf = new StringBuilder();
            buf.append( a ).append( '\n' );
            buf.append( b ).append( '\n' );
            buf.append( c ).append( '\n' );
            buf.append( d1 ).append( '\n' );
            buf.append( d2 ).append( '\n' );
            buf.append( e ).append( '\n' );
            buf.append( f ).append( '\n' );
            buf.append( g ).append( '\n' );
            return buf.toString();
        }
    }

    public void testRequirement()
    {
        System.out.println( injector.getInstance( Component.class ) );
    }
}
