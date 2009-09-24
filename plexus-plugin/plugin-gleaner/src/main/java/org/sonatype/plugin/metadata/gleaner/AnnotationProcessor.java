package org.sonatype.plugin.metadata.gleaner;

import java.util.Map;

public interface AnnotationProcessor
{
    /**
     * Processess one class.
     * 
     * @param className the FQN of the class to process
     * @param classLoader the classloader to use to load it up
     * @param listeners listeners used during processing
     * @param ignoreNotFoundInterfaces if true, interfaces not found in classloader that are implemented by class will
     *            be ignored, otherwise reported as error
     * @throws GleanerException
     */
    void processClass( String className, ClassLoader classLoader, Map<Class<?>, AnnotationListener> listeners,
                       boolean ignoreNotFoundInterfaces )
        throws GleanerException;
}
