package org.sonatype.plugin.metadata.gleaner;

import java.lang.annotation.Annotation;
import java.util.Map;


public interface AnnotationProcessor
{

    void processClass( String className, ClassLoader classLoader, Map<Class<?>, AnnotationListener> listeners )
        throws GleanerException;

}
