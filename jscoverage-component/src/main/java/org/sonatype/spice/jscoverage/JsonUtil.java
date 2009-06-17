package org.sonatype.spice.jscoverage;

import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsonUtil
{

    @SuppressWarnings("unchecked")
    public static JSONObject convertToReportJson( String json )
    {
        JSONObject jsonObject = JSONObject.fromObject( json );

        JSONObject result = new JSONObject();

        Set<String> keys = jsonObject.keySet();
        for ( String jsFile : keys )
        {
            JSONArray arr = (JSONArray) jsonObject.get( jsFile );

            JSONObject coverageResult = new JSONObject();
            coverageResult.put( "coverage", arr );
            coverageResult.put( "source", new JSONArray() );

            result.put( jsFile, coverageResult );
        }

        return result;
    }

}
