package org.sonatype.spice.jscoverage;

import java.io.IOException;

import junit.framework.TestCase;
import net.sf.json.JSONObject;

import org.codehaus.plexus.util.IOUtil;

public class JsonUtilTest
    extends TestCase
{

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    public void testConvertToReportJson()
    {
        String source =
            "{\"http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT\":[null,3,1]}";
        String goal =
            "{\"http://localhost:8084/nexus/js/repoServer/nexus-lvo-plugin-all.js?1.4.0-SNAPSHOT\":{\"coverage\":[null,3,1],\"source\":[]}}";

        String converted = JsonUtil.convertToReportJson( source ).toString();

        assertEquals( goal, converted );
    }

    public void testExtraLoad()
        throws IOException
    {
        String json = IOUtil.toString( getClass().getResourceAsStream( "/jscoverage.json.sample" ) );
        JSONObject result = JsonUtil.convertToReportJson( json );

        assertEquals( 45, result.size() );
    }

}
