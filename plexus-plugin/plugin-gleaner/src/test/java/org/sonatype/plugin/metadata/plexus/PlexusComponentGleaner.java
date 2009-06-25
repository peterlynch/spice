package org.sonatype.plugin.metadata.plexus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plugin.metadata.gleaner.AnnotationListener;
import org.sonatype.plugin.metadata.gleaner.AnnotationProcessor;
import org.sonatype.plugin.metadata.gleaner.ComponentListCreatingAnnotationListener;
import org.sonatype.plugin.metadata.gleaner.DefaultAnnotationProcessor;
import org.sonatype.plugin.metadata.gleaner.GleanerException;
import org.sonatype.plugins.mock.MockExtensionPoint;
import org.sonatype.plugins.mock.MockManaged;
import org.sonatype.reflect.AnnClass;
import org.sonatype.reflect.AnnField;
import org.sonatype.reflect.AnnReader;

public class PlexusComponentGleaner
{

    List<Class<?>> componentMarkingAnnotations;

    public PlexusComponentGleaner( List<Class<?>> componentMarkingAnnotations )
    {
        this.componentMarkingAnnotations = componentMarkingAnnotations;
    }

    public ComponentDescriptor<?> glean( String className, ClassLoader classLoader )
        throws GleanerException,
            IOException
    {
        assert className != null;
        assert classLoader != null;

        AnnClass annClass;
        try
        {
            annClass = readClassAnnotation( className.replace( '.', '/' ) + ".class", classLoader );
        }
        catch ( IOException e )
        {
            throw new GleanerException( "Failed to glean class: " + className );
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

        for ( Class<?> annotationClass : this.componentMarkingAnnotations )
        {
            listenerMap.put( annotationClass, listener );
        }

        annotationProcessor.processClass( className, classLoader, listenerMap );
        if ( listener.getComponentClassNames().isEmpty() )
        {
            // not a component
            return null;
        }

        ComponentDescriptor<?> component = new ComponentDescriptor<Object>();

        try
        {
            String role = this.getComponentsRole( annClass, classLoader );
            component.setRole( role );
        }
        catch ( IOException e )
        {
            throw new GleanerException( "Failed to load " );
        }

        component.setImplementation( className );

        Named namedAnno = annClass.getAnnotation( Named.class );
        if ( namedAnno != null )
        {
            component.setRoleHint( filterEmptyAsNull( namedAnno.value() ) );
        }

        // TODO: add this
        // check singleton
        // component.setInstantiationStrategy( filterEmptyAsNull( anno.instantiationStrategy() ) );

        for ( AnnClass c : getClasses( annClass, classLoader ) )
        {
            for ( AnnField field : c.getFields().values() )
            {
                ComponentRequirement requirement = findRequirement( field, c, classLoader );

                if ( requirement != null )
                {
                    component.addRequirement( requirement );
                }

                // PlexusConfiguration config = findConfiguration( field, c, cl );
                //
                // if ( config != null )
                // {
                // addChildConfiguration( component, config );
                // }
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
            // TODO Auto-generated catch block
            throw new GleanerException( "Can't load class " + fieldType );
        }

        ComponentRequirement requirement;

        requirement = new ComponentRequirement();

        // use the field type as the Role
        requirement.setRole( type.getName() );

        Named namedAnno = field.getAnnotation( Named.class );

        if ( injectAnno == null )
        {
            requirement.setRoleHint( filterEmptyAsNull( namedAnno.value() ) );
        }

        requirement.setFieldName( field.getName() );

        requirement.setFieldMappingType( type.getName() );

        return requirement;
    }

    private String getComponentsRole( AnnClass annClass, ClassLoader classLoader )
        throws IOException
    {
        List<Class<?>> possibleRoleAnnotations = new ArrayList<Class<?>>();
        possibleRoleAnnotations.add( MockExtensionPoint.class );
        possibleRoleAnnotations.add( MockManaged.class );

        // check the class itself first

        for ( Class<?> roleAnnotationClass : possibleRoleAnnotations )
        {
            Object roleAnnotation = annClass.getAnnotation( roleAnnotationClass );
            if ( roleAnnotation != null )
            {
                return annClass.getName().replaceAll( "/", "." );
            }
        }

        for ( String interfaceName : annClass.getInterfaces() )
        {
            AnnClass annInterface = this.readClassAnnotation( interfaceName + ".class", classLoader );

            for ( Class<?> roleAnnotationClass : possibleRoleAnnotations )
            {
                Object roleAnnotation = annInterface.getAnnotation( roleAnnotationClass );
                if ( roleAnnotation != null )
                {
                    return annInterface.getName().replaceAll( "/", "." );
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
                annClass = this.readClassAnnotation( annClass.getSuperName(), classLoader );
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
