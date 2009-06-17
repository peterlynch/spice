package org.sonatype.spice.jscoverage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

public class JsonReportHandlerTest
    extends PlexusTestCase
{

    private DefaultJsonReportHandler handler;

    private File resultFile;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        handler = (DefaultJsonReportHandler) lookup( JsonReportHandler.class );

        resultFile = new File( "jscoverage.json.result" );
    }

    public void testAppendResults()
    {
        handler.appendResults( "{\"http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT\":[null,null,null]}" );
        assertTrue( Arrays.equals( new Integer[] { null, null, null }, getResult() ) );

        handler.appendResults( "{\"http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT\":[null,3,1]}" );
        assertTrue( Arrays.equals( new Integer[] { null, 3, 1 }, getResult() ) );

        handler.appendResults( "{\"http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT\":[null,3,1]}" );
        assertTrue( Arrays.equals( new Integer[] { null, 6, 2 }, getResult() ) );

        handler.appendResults( "{\"http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT\":[1,null,1]}" );
        assertTrue( Arrays.equals( new Integer[] { 1, 6, 3 }, getResult() ) );

        handler.appendResults( "{\"http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT\":[null,null,null]}" );
        assertTrue( Arrays.equals( new Integer[] { 1, 6, 3 }, getResult() ) );

        handler.appendResults( "{\"http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT\":[1,1,1]}" );
        assertTrue( Arrays.equals( new Integer[] { 2, 7, 4 }, getResult() ) );

        handler.appendResults( "{\"http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT\":[5,0,3]}" );
        assertTrue( Arrays.equals( new Integer[] { 7, 7, 7 }, getResult() ) );
    }

    private Integer[] getResult()
    {
        JSONObject result =
            (JSONObject) handler.getResults().get(
                                                   "http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT" );
        JSONArray jsonResult = result.getJSONArray( "coverage" );
        Integer[] arrResult = new Integer[jsonResult.size()];
        for ( int i = 0; i < arrResult.length; i++ )
        {
            Object value = jsonResult.get( i );
            if ( value instanceof Number )
            {
                arrResult[i] = ( (Number) value ).intValue();
            }
        }

        return arrResult;
    }

    public void testPersist()
        throws IOException
    {
        handler.appendResults( "{\"http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT\":[null,null,null]}" );
        handler.appendResults( "{\"http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT\":[1,1,1]}" );

        handler.persist();

        assertTrue( resultFile.exists() );
        assertTrue( resultFile.length() != 0 );
    }

    public void testExtraLoad()
        throws IOException
    {
        String json = IOUtil.toString( getClass().getResourceAsStream( "/jscoverage.json.sample" ) );
        handler.appendResults( json );

        handler.persist();

        assertTrue( resultFile.exists() );
        assertTrue( resultFile.length() != 0 );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        FileUtils.forceDelete( resultFile );
    }
}
