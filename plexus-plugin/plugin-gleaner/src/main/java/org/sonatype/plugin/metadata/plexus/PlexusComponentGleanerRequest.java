package org.sonatype.plugin.metadata.plexus;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;

public class PlexusComponentGleanerRequest
{
    private String className;

    private ClassLoader classRealm;

    private Set<Class<?>> singularComponentAnnotations;

    private Set<Class<?>> pluralComponentAnnotations;

    public PlexusComponentGleanerRequest( String className, ClassLoader classRealm )
    {
        this.className = className;

        this.classRealm = classRealm;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName( String className )
    {
        this.className = className;
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
}
