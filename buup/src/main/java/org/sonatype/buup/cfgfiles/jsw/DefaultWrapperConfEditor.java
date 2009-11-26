package org.sonatype.buup.cfgfiles.jsw;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.buup.cfgfiles.PropertiesFile;

/**
 * Default implementation of WrapperEditor, supporting specific kets of JSW 3.2.x
 * 
 * @author cstamas
 */
public class DefaultWrapperConfEditor
    implements WrapperConfEditor
{
    private static final String WRAPPER_STARTUP_TIMEOUT = "wrapper.startup.timeout";

    private static final int WRAPPER_STARTUP_TIMEOUT_DEFAULT = 30;

    private static final String WRAPPER_SHUTDOWN_TIMEOUT = "wrapper.shutdown.timeout";

    private static final int WRAPPER_SHUTDOWN_TIMEOUT_DEFAULT = 30;

    private static final String WRAPPER_JAVA_MAINCLASS = "wrapper.java.mainclass";

    private static final String WRAPPER_JAVA_CLASSPATH = "wrapper.java.classpath";

    private static final String WRAPPER_JAVA_ADDITIONAL = "wrapper.java.additional";

    private final PropertiesFile wrapperConfWrapper;

    public DefaultWrapperConfEditor( PropertiesFile wrappedWrapper )
    {
        this.wrapperConfWrapper = wrappedWrapper;
    }

    public void reset()
        throws IOException
    {
        wrapperConfWrapper.reset();
    }

    public void save()
        throws IOException
    {
        wrapperConfWrapper.save();
    }

    public void save( File target )
        throws IOException
    {
        wrapperConfWrapper.save( target );
    }

    public PropertiesFile getWrapperConfWrapper()
    {
        return wrapperConfWrapper;
    }

    public int getWrapperStartupTimeout()
    {
        return wrapperConfWrapper.getIntegerProperty( WRAPPER_STARTUP_TIMEOUT, WRAPPER_STARTUP_TIMEOUT_DEFAULT );
    }

    public void setWrapperStartupTimeout( int seconds )
    {
        wrapperConfWrapper.setIntegerProperty( WRAPPER_STARTUP_TIMEOUT, seconds );
    }

    public int getWrapperShutdownTimeout()
    {
        return wrapperConfWrapper.getIntegerProperty( WRAPPER_SHUTDOWN_TIMEOUT, WRAPPER_SHUTDOWN_TIMEOUT_DEFAULT );
    }

    public void setWrapperShutdownTimeout( int seconds )
    {
        wrapperConfWrapper.setIntegerProperty( WRAPPER_SHUTDOWN_TIMEOUT, seconds );
    }

    public String getWrapperJavaMainclass()
    {
        return wrapperConfWrapper.getProperty( WRAPPER_JAVA_MAINCLASS );
    }

    public void setWrapperJavaMainclass( String cls )
    {
        wrapperConfWrapper.setProperty( WRAPPER_JAVA_MAINCLASS, cls );
    }

    public List<String> getWrapperJavaClasspath()
    {
        return wrapperConfWrapper.getPropertyList( WRAPPER_JAVA_CLASSPATH );
    }

    public boolean addWrapperJavaClasspath( String classpathElem )
    {
        List<String> classpath = getWrapperJavaClasspath();

        if ( classpath == null )
        {
            classpath = new ArrayList<String>();
        }

        boolean result = classpath.add( classpathElem );

        setWrapperJavaClasspath( classpath );

        return result;
    }

    public boolean removeWrapperJavaClasspath( String classpathElem )
    {
        List<String> classpath = getWrapperJavaClasspath();

        if ( classpath == null )
        {
            classpath = new ArrayList<String>();
        }

        boolean result = classpath.remove( classpathElem );

        setWrapperJavaClasspath( classpath );

        return result;
    }

    public void setWrapperJavaClasspath( List<String> classpathElems )
    {
        wrapperConfWrapper.setPropertyList( WRAPPER_JAVA_CLASSPATH, classpathElems );
    }

    public List<String> getWrapperJavaAdditional()
    {
        return wrapperConfWrapper.getPropertyList( WRAPPER_JAVA_ADDITIONAL );
    }

    public boolean addWrapperJavaAdditional( String additionalElem )
    {
        List<String> classpath = getWrapperJavaAdditional();

        if ( classpath == null )
        {
            classpath = new ArrayList<String>();
        }

        boolean result = classpath.add( additionalElem );

        setWrapperJavaAdditional( classpath );

        return result;
    }

    public boolean removeWrapperJavaAdditional( String additionalElem )
    {
        List<String> classpath = getWrapperJavaAdditional();

        if ( classpath == null )
        {
            classpath = new ArrayList<String>();
        }

        boolean result = classpath.remove( additionalElem );

        setWrapperJavaAdditional( classpath );

        return result;
    }

    public void setWrapperJavaAdditional( List<String> additionalElems )
    {
        wrapperConfWrapper.setPropertyList( WRAPPER_JAVA_ADDITIONAL, additionalElems );
    }
}
