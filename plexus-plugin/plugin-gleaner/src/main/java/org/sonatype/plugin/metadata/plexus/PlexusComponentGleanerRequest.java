package org.sonatype.plugin.metadata.plexus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;

public class PlexusComponentGleanerRequest
{
    private String classCanonicalName;

    private String classBinaryName;

    private ClassLoader classRealm;

    private Set<Class<?>> singularComponentAnnotations;

    private Set<Class<?>> pluralComponentAnnotations;

    private List<Class<?>> markerAnnotations;

    private boolean ignoreNotFoundImplementedInterfaces;

    /**
     * For backward compatibility. The canonical->binary name transalation is not possible.
     * 
     * @param classCanonicalName
     * @param classRealm
     * @deprecated use the full constructor
     */
    public PlexusComponentGleanerRequest( String classCanonicalName, ClassLoader classRealm )
    {
        this( classCanonicalName, classCanonicalName, classRealm );
    }

    public PlexusComponentGleanerRequest( String classCanonicalName, String classBinaryName, ClassLoader classRealm )
    {
        this.classCanonicalName = classCanonicalName;

        this.classBinaryName = classBinaryName;

        this.classRealm = classRealm;
    }

    public String getClassName()
    {
        return classCanonicalName;
    }

    /**
     * This is here for backward compatibility only. Use the setClassBinaryName() instead!
     * 
     * @param className
     * @deprecated use setClassBinaryName()
     */
    public void setClassName( String className )
    {
        if ( className.contains( "/" ) || className.endsWith( ".class" ) )
        {
            setClassBinaryName( className );
        }
        else
        {
            this.classCanonicalName = className;

            // best effort, but is wrong since no inner classes will work
            this.classBinaryName = className.replace( '.', '/' ) + ".class";
        }
    }

    public String getClassBinaryName()
    {
        return classBinaryName;
    }

    public void setClassBinaryName( String binaryName )
    {
        // convert binary to canonical

        // sanity check
        if ( binaryName == null || binaryName.trim().length() == 0 )
        {
            throw new IllegalArgumentException( "Class binary name cannot be null!" );
        }

        if ( binaryName.endsWith( ".class" ) )
        {
            int startIdx = 0;

            if ( binaryName.startsWith( "/" ) )
            {
                startIdx = 1;
            }

            this.classBinaryName = binaryName;

            this.classCanonicalName =
                binaryName.substring( startIdx, binaryName.length() - 6 ).replace( "/", "." ).replace( "$", "." );
        }
        else
        {
            throw new IllegalArgumentException( "This is not a binary class name: \"" + binaryName + "\"!" );
        }
    }

    public ClassLoader getClassRealm()
    {
        return classRealm;
    }

    public void setClassRealm( ClassLoader classRealm )
    {
        this.classRealm = classRealm;
    }

    public Set<Class<?>> getSingularComponentAnnotations()
    {
        if ( singularComponentAnnotations == null )
        {
            singularComponentAnnotations = new HashSet<Class<?>>();

            singularComponentAnnotations.add( Managed.class );
        }

        return singularComponentAnnotations;
    }

    public Set<Class<?>> getPluralComponentAnnotations()
    {
        if ( pluralComponentAnnotations == null )
        {
            pluralComponentAnnotations = new HashSet<Class<?>>();

            pluralComponentAnnotations.add( ExtensionPoint.class );
        }

        return pluralComponentAnnotations;
    }

    public Set<Class<?>> getComponentAnnotations()
    {
        HashSet<Class<?>> result =
            new HashSet<Class<?>>( getSingularComponentAnnotations().size() + getPluralComponentAnnotations().size() );

        result.addAll( getSingularComponentAnnotations() );

        result.addAll( getPluralComponentAnnotations() );

        return Collections.unmodifiableSet( result );
    }

    public List<Class<?>> getMarkerAnnotations()
    {
        if ( markerAnnotations == null )
        {
            markerAnnotations = new ArrayList<Class<?>>();
        }

        return markerAnnotations;
    }

    public boolean isIgnoreNotFoundImplementedInterfaces()
    {
        return ignoreNotFoundImplementedInterfaces;
    }

    public void setIgnoreNotFoundImplementedInterfaces( boolean ignoreNotFoundImplementedInterfaces )
    {
        this.ignoreNotFoundImplementedInterfaces = ignoreNotFoundImplementedInterfaces;
    }
}
