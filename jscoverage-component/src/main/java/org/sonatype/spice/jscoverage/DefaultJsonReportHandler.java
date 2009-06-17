package org.sonatype.spice.jscoverage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

@Component( role = JsonReportHandler.class, instantiationStrategy = "per-lookup" )
public class DefaultJsonReportHandler
    implements JsonReportHandler, Initializable, Disposable
{

    @Configuration( value = "${basedir}/jscoverage.json.result" )
    private File persistedResults;

    private JSONObject results;

    @SuppressWarnings( "unchecked" )
    public void appendResults( String json )
    {
        JSONObject results = JsonUtil.convertToReportJson( json );

        Set<String> keys = results.keySet();
        for ( String jsFile : keys )
        {
            JSONObject result = (JSONObject) results.get( jsFile );
            if ( !this.results.containsKey( jsFile ) )
            {
                this.results.put( jsFile, result );
            }
            else
            {
                JSONObject currentResult = this.results.getJSONObject( jsFile );

                JSONArray newCoverage = result.getJSONArray( "coverage" );
                JSONArray currentCoverage = currentResult.getJSONArray( "coverage" );

                if ( newCoverage.size() != currentCoverage.size() )
                {
                    throw new IllegalArgumentException( "Coverage results size doesn't match!" );
                }

                for ( int i = 0; i < newCoverage.size(); i++ )
                {
                    Object ncr = newCoverage.get( i );
                    Integer newValue = null;
                    if ( ncr instanceof Number )
                    {
                        newValue = ( (Number) ncr ).intValue();
                    }

                    Object cr = currentCoverage.get( i );
                    if ( cr instanceof Number )
                    {
                        if ( newValue == null )
                        {
                            newValue = ( (Number) cr ).intValue();
                        }
                        else
                        {
                            newValue += ( (Number) cr ).intValue();
                        }
                    }

                    if ( newValue == null )
                    {
                        currentCoverage.set( i, new JSONObject( true ) );
                    }
                    else
                    {
                        currentCoverage.set( i, newValue );
                    }
                }
            }
        }
    }

    public JSONObject getResults()
    {
        return results;
    }

    public void persist()
        throws IOException
    {
        FileOutputStream output = new FileOutputStream( persistedResults );
        try
        {
            IOUtil.copy( results.toString(), output );
        }
        finally
        {
            IOUtil.close( output );
        }
    }

    public void initialize()
        throws InitializationException
    {
        if ( persistedResults.exists() )
        {
            try
            {
                String content = FileUtils.fileRead( persistedResults );
                results = JSONObject.fromObject( content );
            }
            catch ( IOException e )
            {
                throw new InitializationException( "Unable to read persisted results " + persistedResults, e );
            }
        }
        else
        {
            results = new JSONObject();
        }
    }

    public void dispose()
    {
        try
        {
            persist();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

}
