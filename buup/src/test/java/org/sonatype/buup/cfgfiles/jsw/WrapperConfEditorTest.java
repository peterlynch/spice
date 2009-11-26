package org.sonatype.buup.cfgfiles.jsw;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.buup.WrapperHelper;

public class WrapperConfEditorTest
    extends TestCase
{
    private WrapperHelper wrapperHelper;

    private WrapperConfEditor wrapperConfEditor;

    protected WrapperConfEditor prepareCase( String caseNum )
        throws IOException
    {
        File basedir = new File( "target/test-classes/jsw/" + caseNum );

        File source = new File( basedir, "wrapper.conf" );

        FileUtils.copyFile( new File( "src/test/resources/jsw/" + caseNum + "/wrapper.conf" ), source );

        wrapperHelper = new WrapperHelper( basedir );

        wrapperConfEditor = wrapperHelper.getWrapperEditor( source );

        return wrapperConfEditor;
    }

    protected void validateCase( String caseNum )
        throws IOException
    {
        File source = new File( "target/test-classes/jsw/" + caseNum + "/wrapper.conf" );

        File expectedResult = new File( "src/test/resources/jsw/" + caseNum + "/wrapper.conf.result" );

        assertEquals( "wrapper.conf does not match!", FileUtils.fileRead( expectedResult ), FileUtils.fileRead( source ) );
    }

    public void testCase01()
        throws Exception
    {
        WrapperConfEditor editor = prepareCase( "c01" );

        assertEquals( "The startup timeout does not match!", 90, editor.getWrapperStartupTimeout() );

        Map<String, String> allKeyValuePairs = editor.getWrapperConfWrapper().getAllKeyValuePairs();

        assertEquals( "Not all entries are read!", 24, allKeyValuePairs.size() );

        editor.setWrapperStartupTimeout( 130 );

        String mainClass = editor.getWrapperJavaMainclass();

        assertEquals( "Main class does not match!", "org.sonatype.sample.wrapper.ApplicationA", mainClass );

        editor.setWrapperJavaMainclass( mainClass.substring( 0, mainClass.length() - 1 ) + "B" );
        
        List<String> classpathElems = editor.getWrapperJavaClasspath();
        
        assertEquals("Classpath not loaded properly", 2, classpathElems.size());
        assertEquals("Classpath not loaded properly", "../../../lib/*.jar", classpathElems.get( 0 ));
        assertEquals("Classpath not loaded properly", "../../../conf/", classpathElems.get( 1 ));
        
        editor.addWrapperJavaClasspath( "../../foo/bar.jar" );
        
        editor.addWrapperJavaAdditional( "-DthisIsAFooProperty=foo" );
        
        editor.save();

        validateCase( "c01" );
    }
}
