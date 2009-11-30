package org.sonatype.buup.cfgfiles.jsw;

import java.util.List;

import org.sonatype.buup.cfgfiles.EditableFile;
import org.sonatype.buup.cfgfiles.PropertiesFile;

/**
 * "High level" wrapper.conf editor, that uses WrapperConfWrapper and provides high-level editing capabilities but
 * keeping user made changed to the file.
 * 
 * @author cstamas
 */
public interface WrapperConfEditor
    extends EditableFile
{
    /**
     * Returns the PropertiesFile instance that this Editor is backed with.
     * 
     * @return
     */
    PropertiesFile getWrapperConfWrapper();

    /**
     * Returns the wrapper startup timeout in seconds.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-startup-timeout.html.
     * @return
     */
    int getWrapperStartupTimeout();

    /**
     * Sets the wrapper startup timeout in seconds.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-startup-timeout.html.
     * @param seconds the timeout in seconds
     */
    void setWrapperStartupTimeout( int seconds );

    /**
     * Returns the wrapper shutdown timeout in seconds.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-shutdown-timeout.html.
     * @return
     */
    int getWrapperShutdownTimeout();

    /**
     * Sets the wrapper shutdown timeout in seconds.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-shutdown-timeout.html.
     * @param seconds the timeout in seconds
     */
    void setWrapperShutdownTimeout( int seconds );

    /**
     * Returns the wrapper java main class.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-mainclass.html.
     * @return
     */
    String getWrapperJavaMainclass();

    /**
     * Sets the wrapper java main class.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-mainclass.html.
     * @param class to be executed by JSW brought up JVM
     */
    void setWrapperJavaMainclass( String cls );

    /**
     * Returns a changeable list of wrapper.java.classpath settings. The order in list follows wrapper.conf "notation":
     * 
     * <pre>
     * wrapper.java.classpath.1=../../../lib/*.jar
     * wrapper.java.classpath.2=../../../conf/
     * </pre>
     * 
     * Note: First index is 1, not 0, but in Java List implementation, we follow the Java-way, hence
     * wrapper.java.classpath.1 becomes 1st element of the returned list on index 0. Changes to this list are <b>not</b>
     * persisted! Use setWrapperJavaClasspath() to persist it!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-classpath-n.html.
     * @return
     */
    List<String> getWrapperJavaClasspath();

    /**
     * Adds a new element to wrapper.conf wrapper.java.classpath configuration, to the last position. First index is 1,
     * not 0!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-classpath-n.html.
     * @param classpathElem
     * @return true
     */
    boolean addWrapperJavaClasspath( String classpathElem );

    /**
     * Removes 1st occurence of the provided parameter from wrapper.conf wrapper.java.classpath list, potentionally
     * shiting up all elements after it from pos to pos-1. First index is 1, not 0!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-classpath-n.html.
     * @param classpathElem
     * @return true if elem found and removed
     */
    boolean removeWrapperJavaClasspath( String classpathElem );

    /**
     * Replaces all elements in wrapper.conf wrapper.java.classpath configuration.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-classpath-n.html.
     * @param pos
     * @param classpathElem
     */
    void setWrapperJavaClasspath( List<String> classpathElems );

    /**
     * Returns a changeable list of wrapper.java.additional settings. The order in list follows wrapper.conf "notation":
     * 
     * <pre>
     * wrapper.java.additional.1=-d32
     * wrapper.java.additional.2=-Xmx512
     * </pre>
     * 
     * Note: First index is 1, not 0, but in Java List implementation, we follow the Java-way, hence
     * wrapper.java.additional.1 becomes 1st element of the returned list on index 0. Changes to this list are
     * <b>not</b> persisted! Use setWrapperJavaAdditional() to persist it!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-additional-n.html.
     * @return
     */
    List<String> getWrapperJavaAdditional();

    /**
     * Adds a new element to wrapper.conf wrapper.java.classpath configuration, to the last position. First index is 1,
     * not 0!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-additional-n.html.
     * @param additionalElem
     * @return true
     */
    boolean addWrapperJavaAdditional( String additionalElem );

    /**
     * Removes 1st occurence of the provided parameter from wrapper.conf wrapper.java.classpath list, potentionally
     * shiting up all elements after it from pos to pos-1. First index is 1, not 0!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-additional-n.html.
     * @param additionalElem
     * @return true if elem found and removed
     */
    boolean removeWrapperJavaAdditional( String additionalElem );

    /**
     * Replaces all elements in wrapper.conf wrapper.java.classpath configuration.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-additional-n.html.
     * @param pos
     * @param additionalElems
     */
    void setWrapperJavaAdditional( List<String> additionalElems );

    /**
     * The exit code that maps to JSW's "default". To be used with methods getWrapperOnExit() and setWrapperOnExit().
     */
    int DEFAULT_EXIT_CODE = -1;

    /**
     * Gets the exit command for given exit code.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-on-exit-n.html
     * @return
     */
    OnExitCommand getWrapperOnExit( int exitCode );

    /**
     * Sets the exit command for given exit code.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-on-exit-n.html
     * @param exitCode
     * @param cmd
     */
    void setWrapperOnExit( int exitCode, OnExitCommand cmd );

    /**
     * Gets the reload configuration settings of wrapper.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-restart-reload-configuration.html
     * @return
     */
    boolean getWrapperRestartReloadConfiguration();

    /**
     * Sets the reload settings of wrapper
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-restart-reload-configuration.html
     * @param reload
     */
    void setWrapperRestartReloadConfiguration( boolean reload );

}
