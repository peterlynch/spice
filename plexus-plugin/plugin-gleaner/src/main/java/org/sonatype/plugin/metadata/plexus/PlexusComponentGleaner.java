package org.sonatype.plugin.metadata.plexus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plugin.metadata.gleaner.AnnotationListener;
import org.sonatype.plugin.metadata.gleaner.AnnotationProcessor;
import org.sonatype.plugin.metadata.gleaner.ComponentListCreatingAnnotationListener;
import org.sonatype.plugin.metadata.gleaner.DefaultAnnotationProcessor;
import org.sonatype.plugin.metadata.gleaner.GleanerException;
import org.sonatype.reflect.AnnClass;
import org.sonatype.reflect.AnnField;
import org.sonatype.reflect.AnnReader;

/**
 * This is dirty and hackish. But works for now. The trick is that this gleaner is able to process the class, create a
 * Plexus specific ComponentDescriptor for it, and also that we are able to "drive" is the component in case a
 * "singular" or "plural" case.
 * 
 * @author toby
 * @author cstamas
 */
@Component( role = PlexusComponentGleaner.class )
public class PlexusComponentGleaner
{
    public ComponentDescriptor<?> glean( PlexusComponentGleanerRequest request )
        throws GleanerException, IOException
    {
        AnnClass annClass;

        try
        {
            annClass =
                readClassAnnotation( request.getClassName().replace( '.', '/' ) + ".class", request.getClassRealm() );
        }
        catch ( IOException e )
        {
            throw new GleanerException( "Failed to glean class: " + request.getClassName() );
        }
        
        if( annClass == null )
        {
            throw new GleanerException( "Failed to glean class: " + request.getClassName() );
        }

        // Skip abstract classes
        if ( Modifier.isAbstract( annClass.getAccess() ) )
        {
            return null;
        }

        // check if it is component
        AnnotationProcessor annotationProcessor = new DefaultAnnotationProcessor();
        ComponentListCreatingAnnotationListener listener = new ComponentListCreatingAnnotationListener();
        Map<Class<?>, AnnotationListener> listenerMap = new HashMap<Class<?>, AnnotationListener>();

        for ( Class<?> annotationClass : request.getComponentAnnotations() )
        {
            listenerMap.put( annotationClass, listener );
        }

        annotationProcessor.processClass( request.getClassName(), request.getClassRealm(), listenerMap );

        if ( listener.getComponentClassNames().isEmpty() )
        {
            // not a component
            return null;
        }

        boolean isSingular = false;

        AnnClass role = null;

        try
        {
            // try to seek for plural
            role = getComponentsRole( request.getPluralComponentAnnotations(), request.getClassRealm(), annClass );

            if ( role == null )
            {
                // try singular
                role = getComponentsRole( request.getSingularComponentAnnotations(), request.getClassRealm(), annClass );

                isSingular = true;
            }
        }
        catch ( IOException e )
        {
            throw new GleanerException( "Failed to load " );
        }

        if ( role == null )
        {
            // humm? we checked this already above (is it a component)
            return null;
        }

        ComponentDescriptor<?> component = new ComponentDescriptor<Object>();

        component.setRole( role.getName().replaceAll( "/", "." ) );

        component.setImplementation( request.getClassName() );

        // now a little game: @Named anno always wins, if developer specifies it
        // otherwise, we have "singular" component type: one role, one imple, aka Plexus "default" role hint
        // or, "plural" component type: one role, many imple, aka Plexus non-default role hint
        // usually, the 1st case will be user components (own private but managed stuff), 2nd case will
        // be @ExtensionPoint
        Named namedAnno = annClass.getAnnotation( Named.class );
        if ( namedAnno != null )
        {
            component.setRoleHint( filterEmptyAsNull( namedAnno.value() ) );
        }
        else
        {
            if ( !isSingular )
            {
                component.setRoleHint( component.getImplementation() );
            }
        }

        // honor the Singleton
        Singleton singletonAnno = role.getAnnotation( Singleton.class );
        if ( singletonAnno == null )
        {
            component.setInstantiationStrategy( "per-lookup" );
        }

        for ( AnnClass c : getClasses( annClass, request.getClassRealm() ) )
        {
            for ( AnnField field : c.getFields().values() )
            {
                ComponentRequirement requirement = findRequirement( field, c, request.getClassRealm() );

                if ( requirement != null )
                {
                    component.addRequirement( requirement );
                }
            }
        }

        return component;
    }

    private ComponentRequirement findRequirement( final AnnField field, AnnClass annClass, ClassLoader cl )
        throws GleanerException
    {
        assert field != null;

        Inject injectAnno = field.getAnnotation( Inject.class );

        if ( injectAnno == null )
        {
            return null;
        }

        String fieldType = field.getType();

        // TODO implement type resolution without loading classes
        Class<?> type;
        try
        {
            type = Class.forName( fieldType, false, cl );
        }
        catch ( ClassNotFoundException ex )
        {
            throw new GleanerException( "Can't load class " + fieldType );
        }

        ComponentRequirement requirement = new ComponentRequirement();

        // use the field type as the Role
        requirement.setRole( type.getName() );

        Named namedAnno = field.getAnnotation( Named.class );

        if ( namedAnno != null )
        {
            requirement.setRoleHint( filterEmptyAsNull( namedAnno.value() ) );
        }

        requirement.setFieldName( field.getName() );

        requirement.setFieldMappingType( type.getName() );

        return requirement;
    }

    private AnnClass getComponentsRole( Set<Class<?>> annosToLookFor, ClassLoader classloader, AnnClass annClass )
        throws IOException
    {
        // check the class itself first
        for ( Class<?> roleAnnotationClass : annosToLookFor )
        {
            Object roleAnnotation = annClass.getAnnotation( roleAnnotationClass );
            if ( roleAnnotation != null )
            {
                return annClass;
            }
        }

        for ( String interfaceName : annClass.getInterfaces() )
        {
            AnnClass annInterface = this.readClassAnnotation( interfaceName + ".class", classloader );

            for ( Class<?> roleAnnotationClass : annosToLookFor )
            {
                Object roleAnnotation = annInterface.getAnnotation( roleAnnotationClass );
                if ( roleAnnotation != null )
                {
                    return annInterface;
                }
            }
        }

        return null;
    }

    private AnnClass readClassAnnotation( String resourceName, ClassLoader classLoader )
        throws IOException
    {   
        InputStream classStream = null;
        try
        {
            classStream = classLoader.getResourceAsStream( resourceName );

            if ( classStream != null )
            {
                return AnnReader.read( classStream, classLoader );
            }
        }
        finally
        {
            IOUtil.close( classStream );
        }
        return null;
    }

    protected String filterEmptyAsNull( final String value )
    {
        if ( value == null )
        {
            return null;
        }
        else if ( StringUtils.isEmpty( value.trim() ) )
        {
            return null;
        }
        else
        {
            return value;
        }
    }

    /**
     * Returns a list of all of the classes which the given type inherits from.
     * 
     * @throws IOException
     */
    private List<AnnClass> getClasses( AnnClass annClass, ClassLoader classLoader )
        throws IOException
    {
        assert annClass != null;

        List<AnnClass> classes = new ArrayList<AnnClass>();

        while ( annClass != null )
        {
            classes.add( annClass );
            if ( annClass.getSuperName() != null )
            {
                annClass = this.readClassAnnotation( annClass.getSuperName() + ".class", classLoader );
            }
            else
            {
                break;
            }

            //
            // TODO: See if we need to include interfaces here too?
            //
        }

        return classes;
    }

    protected boolean isRequirementListType( final Class<?> type )
    {
        // assert type != null;

        return Collection.class.isAssignableFrom( type ) || Map.class.isAssignableFrom( type );
    }

}
