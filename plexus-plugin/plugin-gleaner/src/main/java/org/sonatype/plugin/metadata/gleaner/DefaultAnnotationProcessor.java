package org.sonatype.plugin.metadata.gleaner;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.reflect.AnnClass;
import org.sonatype.reflect.AnnReader;

public class DefaultAnnotationProcessor
    implements AnnotationProcessor
{
    public void processClass( String className, ClassLoader classLoader, Map<Class<?>, AnnotationListener> listenerMap )
        throws GleanerException
    {
        // fix the classname
        String resourceName = this.classNameToResourceName( className );

        try
        {
            AnnClass annClass = readClassAnnotations( resourceName, classLoader );

            if ( annClass == null )
            {
                throw new GleanerException( "Failed to fine class: " + resourceName );
            }

            // do not process any abstract classes
            if ( Modifier.isAbstract( annClass.getAccess() ) )
            {
                // Abstract classes cannot be components
                return;
            }

            Object annotationInstance = null;
            List<Class<?>> annotations = new ArrayList<Class<?>>();
            List<String> annotationDebugString = new ArrayList<String>();

            // first check 'annotatedClass' for any of the annotations, if there is more then one, this is a problem
            for ( Class<?> annotationClass : listenerMap.keySet() )
            {
                // now look up the expected annotations
                annotationInstance = annClass.getAnnotation( annotationClass );

                if ( annotationInstance != null )
                {
                    annotations.add( annotationClass );
                    annotationDebugString.add( "Annotation: " + annotationClass + " found in class: " + className );
                }

                // check its direct interfaces too
                for ( String interfaceName : annClass.getInterfaces() )
                {
                    AnnClass annotatedInterface =
                        readClassAnnotations( this.classNameToResourceName( interfaceName ), classLoader );
                    if ( annotatedInterface == null )
                    {
                        throw new GleanerException( "Failed to find class: " + interfaceName );
                    }

                    // now look up the expected annotations
                    annotationInstance = annotatedInterface.getAnnotation( annotationClass );

                    if ( annotationInstance != null )
                    {
                        annotations.add( annotationClass );
                        annotationDebugString.add( "Annotation: " + annotationClass + " found in interface: "
                            + interfaceName );
                    }
                }
            }

            // we can only have ONE annotation, if we have more then ONE, we will fail

            if ( annotations.size() > 1 )
            {
                throw new GleanerException( "Component has more then one role, roles found: " + annotationDebugString );
            }

            // now we need to check if we have one at all
            if ( !annotations.isEmpty() )
            {
                Class<?> annotation = annotations.get( 0 );
                // all that work for just one line
                AnnotationListener listener = listenerMap.get( annotation );

                // if this is null we have big problems
                if ( listener == null )
                {
                    throw new IllegalStateException( "Unexpected: Could not find Listener for Annotation: "
                        + annotation.getName() );
                }

                AnnotationListernEvent event = new AnnotationListernEvent( annotationInstance, annClass.getName() );

                listener.processEvent( event );
            }
        }
        catch ( IOException e )
        {
            throw new GleanerException( "Failed to process class: " + className );
        }

    }

    private AnnClass readClassAnnotations( String resourceName, ClassLoader classLoader )
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

    private String classNameToResourceName( String className )
    {
        String resourceName = className;
        // this could all be done with a single regex... if I only knew what that regex would be
        resourceName = resourceName.replaceAll( "\\.class\\z", "" );
        resourceName = resourceName.replaceAll( "\\.", "/" );
        return resourceName + ".class";
    }

}
